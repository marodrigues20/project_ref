# Transaction Manager

To run the service execute the following command at the root of the project.  This will download and install the 
gradle wrapper and then execute the service.

```
> /gradlew run
```

## Generating the Operating Manual

The operating manual is written using the asciidoc format and the source files can be found in the docs directory.
To generate the operating manual the following command is executed in the root of the project.

```
> ./gradlew asciidoc
```

The operating manual will be rendered in HTML and the output can be located in the build/operating-manual directory.

## Backup and Retention Policy

### Redis
The data stored in redis is ephemeral, and not designed to live beyond <TTL>, this backup and retention is not required, as we are happy to lose the stored data in event of disaster.
 

## Running Terraform

* Download and install terraform. Please install the 0.11.7 version and follow instructions on https://www.terraform.io/intro/getting-started/install.html
* Set up the AWS profile for the environment you want to run terraform against in your ~/.aws folder
* Run the following command to point to the desired environment
```
export AWS_PROFILE=<your_aws_profile_name> for e.g. export AWS_PROFILE=devblue
```
* cd to the <project-root>/terraform directory and modify the following line in the backend.tf file
```
bucket = "<correct_bucket_name_based_on_AWS_environment>" for e.g. 10x-solution-test-blue-terraform-state for solution test blue or 10x-devblue-terraform-state for dev blue
```
* cd to the <project-root>/terraform directory and enter the following command when running terraform for the first time or when you import a new module or point to a different AWS environment
```
terraform rm -rf .terraform
terraform init
```
* Run the following command to execute the terraform plan. Executing the plan will display the actions that the terraform will perform on the AWS estate
```
terraform plan -out=<file-path-where-you-want-plan-to-be-stored>
```
* Run the following command to execute the terraform plan. Executing apply will apply the changes displayed on the previous step
```
terraform apply <file-path-where-you-want-plan-to-be-stored>
``