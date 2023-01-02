import * as cdk from "@aws-cdk/core";
import * as s3 from "@aws-cdk/aws-s3";
import * as cloudfront from "@aws-cdk/aws-cloudfront";
import * as origins from "@aws-cdk/aws-cloudfront-origins";
import * as s3deploy from "@aws-cdk/aws-s3-deployment";
import * as acm from "@aws-cdk/aws-certificatemanager";
import * as lambda from "@aws-cdk/aws-lambda";
import { Duration } from "@aws-cdk/core";
import * as iam from "@aws-cdk/aws-iam";
import * as events from "@aws-cdk/aws-events";
import * as eventTargets from "@aws-cdk/aws-events-targets";

export class CdkStack extends cdk.Stack {
  constructor(scope: cdk.Construct, id: string, props?: cdk.StackProps) {
    super(scope, id, props);

    // Web
    const bucket = new s3.Bucket(this, "LunchBreakWeb", {
      cors: [
        {
          allowedMethods: [s3.HttpMethods.GET],
          allowedOrigins: ["*"],
          allowedHeaders: ["*"],
        },
      ],
    });

    const oai = new cloudfront.OriginAccessIdentity(this, "LunchBreakWebOAI");
    bucket.grantRead(oai);

    const distribution = new cloudfront.Distribution(
      this,
      "LunchBreakCloudFront",
      {
        comment: "LunchBreak",
        defaultRootObject: "index.html",
        domainNames: ["lunch.irrlicht.io"],
        defaultBehavior: {
          origin: new origins.S3Origin(bucket, { originAccessIdentity: oai }),
          allowedMethods: cloudfront.AllowedMethods.ALLOW_GET_HEAD_OPTIONS,
        },
        certificate: acm.Certificate.fromCertificateArn(
          this,
          "Certificate",
          "arn:aws:acm:us-east-1:420912396104:certificate/8b452da9-bd92-471b-802f-85cf34b98d6b"
        ),
      }
    );

    new s3deploy.BucketDeployment(this, "LunchBreakDeploymentBucket", {
      sources: [s3deploy.Source.asset("../frontend/public")],
      destinationBucket: bucket,
      prune: false,
      distribution,
      distributionPaths: ["/*"],
    });

    // API
    const apiLambda = new lambda.Function(this, "LunchBreakApiLambda", {
      runtime: lambda.Runtime.JAVA_8,
      handler: "Handler::handleRequest",
      code: lambda.Code.fromAsset("../api/target/lunch-1.0.jar"),
      memorySize: 2048,
      timeout: Duration.seconds(20),
      environment: { bucket: bucket.bucketName },
    });

    apiLambda.addToRolePolicy(
      new iam.PolicyStatement({
        actions: ["s3:PutObject"],
        resources: [bucket.bucketArn + "/*"],
        effect: iam.Effect.ALLOW,
      })
    );

    new events.Rule(this, "LunchTimeScheduler", {
      schedule: events.Schedule.expression("cron(0 5-13 * * ? *)"), // every hour from 5 to 13 (GMT)
      targets: [new eventTargets.LambdaFunction(apiLambda)],
    });
  }
}
