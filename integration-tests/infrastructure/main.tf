terraform {
  required_version = ">= 1.16.0"

  required_providers {
    archive = {
      source  = "hashicorp/archive"
      version = "2.8.1"
    }
    aws = {
      source  = "hashicorp/aws"
      version = "6.66.0"
    }
  }
}

provider "aws" {
  region              = "eu-central-1"
  allowed_account_ids = ["956243129466"]
}

data "aws_caller_identity" "current" {}

data "archive_file" "lambda_function_asset" {
  type        = "zip"
  source_dir  = "${path.module}/../lambda"
  output_path = "${path.module}/../lambda.zip"
}

resource "aws_iam_role" "lambda_function_role" {
  name = "feign-aws-sigv4-integration-tests-lambda-function-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = "sts:AssumeRole"
        Principal = {
          Service = "lambda.amazonaws.com"
        }
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "lambda_function_basic_execution_attachment" {
  role       = aws_iam_role.lambda_function_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"
}

resource "aws_lambda_function" "lambda_function" {
  function_name    = "feign-aws-sigv4-integration-tests-lambda"
  filename         = data.archive_file.lambda_function_asset.output_path
  handler          = "index.handler"
  memory_size      = 128
  runtime          = "nodejs24.x"
  source_code_hash = data.archive_file.lambda_function_asset.output_base64sha256
  timeout          = 5
  role             = aws_iam_role.lambda_function_role.arn
}

resource "aws_apigatewayv2_api" "api_gateway" {
  name          = "feign-aws-sigv4-integration-tests-api-gateway"
  protocol_type = "HTTP"
}

resource "aws_apigatewayv2_integration" "api_gateway_integration" {
  api_id                 = aws_apigatewayv2_api.api_gateway.id
  integration_uri        = aws_lambda_function.lambda_function.invoke_arn
  integration_type       = "AWS_PROXY"
  integration_method     = "POST"
  payload_format_version = "2.0"
}

resource "aws_apigatewayv2_route" "api_gateway_route" {
  api_id             = aws_apigatewayv2_api.api_gateway.id
  route_key          = "$default"
  target             = "integrations/${aws_apigatewayv2_integration.api_gateway_integration.id}"
  authorization_type = "AWS_IAM"
}

resource "aws_lambda_permission" "api_gateway_lambda_permission" {
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.lambda_function.function_name
  principal     = "apigateway.amazonaws.com"
  source_arn    = "${aws_apigatewayv2_api.api_gateway.execution_arn}/*/*"
}

resource "aws_apigatewayv2_stage" "api_gateway_stage" {
  api_id      = aws_apigatewayv2_api.api_gateway.id
  name        = "$default"
  auto_deploy = true
}

resource "aws_iam_role" "integration_test_role" {
  name = "feign-aws-sigv4-integration-tests-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Principal = {
          Federated = "arn:aws:iam::${data.aws_caller_identity.current.account_id}:oidc-provider/token.actions.githubusercontent.com"
        }
        Action = "sts:AssumeRoleWithWebIdentity"
        Condition = {
          StringEquals = {
            "token.actions.githubusercontent.com:aud" = "sts.amazonaws.com"
            "token.actions.githubusercontent.com:sub" = [
              "repo:DarkAtra/feign-aws-sigv4:ref:refs/heads/main",
              "repo:DarkAtra/feign-aws-sigv4:ref:refs/heads/next"
            ]
          }
        }
      }
    ]
  })
}

resource "aws_iam_role_policy" "api_gateway_access" {
  name = "api-gateway-access"
  role = aws_iam_role.integration_test_role.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect   = "Allow"
        Action   = "execute-api:Invoke"
        Resource = "${aws_apigatewayv2_api.api_gateway.execution_arn}/*/*"
      }
    ]
  })
}

output "api_gateway_url" {
  value = aws_apigatewayv2_stage.api_gateway_stage.invoke_url
}

output "integration_test_role_arn" {
  value = aws_iam_role.integration_test_role.arn
}
