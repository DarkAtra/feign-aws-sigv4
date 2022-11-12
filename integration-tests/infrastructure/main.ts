import {Construct} from 'constructs';
import {App, AssetType, TerraformAsset, TerraformOutput, TerraformStack} from 'cdktf';
import {Apigatewayv2Api} from '@cdktf/provider-aws/lib/apigatewayv2-api';
import {AwsProvider} from '@cdktf/provider-aws/lib/provider';
import {LambdaFunction} from '@cdktf/provider-aws/lib/lambda-function';
import {join} from 'path';
import {IamRole} from '@cdktf/provider-aws/lib/iam-role';
import {IamRolePolicyAttachment} from '@cdktf/provider-aws/lib/iam-role-policy-attachment';
import {Apigatewayv2Integration} from '@cdktf/provider-aws/lib/apigatewayv2-integration';
import {Apigatewayv2Route} from '@cdktf/provider-aws/lib/apigatewayv2-route';
import {LambdaPermission} from '@cdktf/provider-aws/lib/lambda-permission';
import {Apigatewayv2Stage} from '@cdktf/provider-aws/lib/apigatewayv2-stage';
import {DataAwsCallerIdentity} from '@cdktf/provider-aws/lib/data-aws-caller-identity';

class IntegrationTestStack extends TerraformStack {

    private callerIdentity: DataAwsCallerIdentity;

    constructor(scope: Construct, private id: string) {
        super(scope, id);

        new AwsProvider(this, 'aws', {
            region: 'eu-central-1'
        });

        this.callerIdentity = new DataAwsCallerIdentity(this, 'caller-identity');

        const lambdaFunction = this.setupLambdaFunction();
        const {apiGateway, apiGatewayStage} = this.setupApiGateway(lambdaFunction);
        const integrationTestRole = this.setupIntegrationTestRole(apiGateway);

        new TerraformOutput(this, 'api-gateway-url', {
            staticId: true,
            value: apiGatewayStage.invokeUrl
        });

        new TerraformOutput(this, 'integration-test-role-arn', {
            staticId: true,
            value: integrationTestRole.arn
        });
    }

    private setupLambdaFunction(): LambdaFunction {

        const lambdaAsset = new TerraformAsset(this, `lambda-function-asset`, {
            path: join(__dirname, 'lambda'),
            type: AssetType.ARCHIVE
        });

        const lambdaRole = new IamRole(this, 'lambda-function-role', {
            name: `${this.id}-lambda-function-role`,
            assumeRolePolicy: JSON.stringify({
                'Version': '2012-10-17',
                'Statement': [
                    {
                        'Effect': 'Allow',
                        'Action': 'sts:AssumeRole',
                        'Principal': {
                            'Service': 'lambda.amazonaws.com'
                        }
                    }
                ]
            })
        });

        new IamRolePolicyAttachment(this, 'lambda-function-policy-attachment', {
            role: lambdaRole.name,
            policyArn: 'arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole'
        });

        return new LambdaFunction(this, `lambda-function`, {
            functionName: `${this.id}-lambda`,
            filename: lambdaAsset.path,
            handler: 'index.handler',
            memorySize: 128,
            runtime: 'nodejs16.x',
            sourceCodeHash: lambdaAsset.assetHash,
            timeout: 5,
            role: lambdaRole.arn
        });
    }

    private setupApiGateway(lambdaFunction: LambdaFunction): { apiGateway: Apigatewayv2Api, apiGatewayStage: Apigatewayv2Stage } {

        const apiGateway = new Apigatewayv2Api(this, 'api-gateway', {
            name: `${this.id}-api-gateway`,
            protocolType: 'HTTP'
        });

        const apiGatewayIntegration: Apigatewayv2Integration = new Apigatewayv2Integration(this, 'api-gateway-integration', {
            apiId: apiGateway.id,
            integrationUri: lambdaFunction.invokeArn,
            integrationType: 'AWS_PROXY',
            integrationMethod: 'POST',
            payloadFormatVersion: '2.0'
        });

        new Apigatewayv2Route(this, 'api-gateway-route', {
            apiId: apiGateway.id,
            routeKey: '$default', // catchall
            target: `integrations/${apiGatewayIntegration.id}`,
            authorizationType: 'AWS_IAM'
        });

        new LambdaPermission(this, 'api-gateway-lambda-permission', {
            action: 'lambda:InvokeFunction',
            functionName: lambdaFunction.functionName,
            principal: 'apigateway.amazonaws.com',
            sourceArn: `${apiGateway.executionArn}/*/*`
        });

        const apiGatewayStage = new Apigatewayv2Stage(this, 'api-gateway-stage', {
            apiId: apiGateway.id,
            name: '$default',
            autoDeploy: true
        });

        return {apiGateway, apiGatewayStage};
    }

    private setupIntegrationTestRole(apiGateway: Apigatewayv2Api): IamRole {

        return new IamRole(this, 'integration-test-role', {
            name: `${this.id}-role`,
            assumeRolePolicy: JSON.stringify({
                'Version': '2012-10-17',
                'Statement': [
                    {
                        'Effect': 'Allow',
                        'Principal': {
                            'Federated': `arn:aws:iam::${this.callerIdentity.accountId}:oidc-provider/token.actions.githubusercontent.com`
                        },
                        'Action': 'sts:AssumeRoleWithWebIdentity',
                        'Condition': {
                            'StringEquals': {
                                'token.actions.githubusercontent.com:aud': 'sts.amazonaws.com',
                                'token.actions.githubusercontent.com:sub': 'repo:DarkAtra/feign-aws-sigv4:ref:refs/heads/main'
                            }
                        }
                    }
                ]
            }),
            inlinePolicy: [
                {
                    name: 'api-gateway-access',
                    policy: JSON.stringify({
                        Version: '2012-10-17',
                        Statement: [{
                            Effect: 'Allow',
                            Action: 'execute-api:Invoke',
                            Resource: [
                                `${apiGateway.executionArn}/*/*`
                            ]
                        }]
                    })
                }
            ]
        });
    }
}

const app = new App();
new IntegrationTestStack(app, 'feign-aws-sigv4-integration-tests');
app.synth();
