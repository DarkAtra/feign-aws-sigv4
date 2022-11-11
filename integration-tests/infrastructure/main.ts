import {Construct} from 'constructs';
import {App, TerraformStack} from 'cdktf';
import {Apigatewayv2Api} from '@cdktf/provider-aws/lib/apigatewayv2-api';
import {AwsProvider} from '@cdktf/provider-aws/lib/provider';

class IntegrationTestStack extends TerraformStack {

    constructor(scope: Construct, id: string) {
        super(scope, id);

        new AwsProvider(this, 'aws', {
            region: 'eu-central-1'
        });

        new Apigatewayv2Api(this, 'api-gateway', {
            name: 'feign-aws-sigv4-integration-test',
            protocolType: 'HTTP'
        });
    }
}

const app = new App();
new IntegrationTestStack(app, 'feign-aws-sigv4-integration-tests');
app.synth();
