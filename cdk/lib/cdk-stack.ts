import * as cdk from "@aws-cdk/core";
import * as s3 from "@aws-cdk/aws-s3";
import * as cloudfront from "@aws-cdk/aws-cloudfront";
import * as s3deploy from "@aws-cdk/aws-s3-deployment";
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
      cors: [{
        allowedMethods: [s3.HttpMethods.GET],
        allowedOrigins: ["*"],
        allowedHeaders: ["*"]
      }]
    });
    const oai = new cloudfront.OriginAccessIdentity(this, "LunchBreakWebOAI");

    bucket.grantRead(oai);

    const distribution = new cloudfront.CloudFrontWebDistribution(
      this,
      "LunchBreakCloudFront",
      {
        originConfigs: [
          {
            s3OriginSource: {
              s3BucketSource: bucket,
              originAccessIdentity: oai,
            },
            behaviors: [{ 
              isDefaultBehavior: true, 
              allowedMethods: cloudfront.CloudFrontAllowedMethods.GET_HEAD_OPTIONS,
              cachedMethods: cloudfront.CloudFrontAllowedCachedMethods.GET_HEAD_OPTIONS,
              forwardedValues: {
                headers: ["Origin", "Access-Control-Request-Headers", "Access-Control-Request-Method"],
                queryString: true,
              }
            }],
          },
        ],
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
      timeout: Duration.seconds(10),
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
