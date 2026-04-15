#!/usr/bin/env python3
"""One-off helper: assign module field to each question. Run from repo root if needed."""
import json
from pathlib import Path

# Must match QuizModules in Kotlin
COMPUTE = "Compute in the Cloud"
GOING_GLOBAL = "Going Global"
NETWORKING = "Networking"
STORAGE = "Storage"
DATABASES = "Databases"
AI_ML_DATA = "AI ML and Data Analytics"
SECURITY = "Security"
MON_GOV = "Monitoring, Compliance and Governance in the AWS Cloud"
PRICING = "Pricing and Support"
MIGRATING = "Migrating to the AWS Cloud"
WELL_ARCH = "Well-Architected Solutions"

# Explicit overrides when prefix rules are ambiguous
OVERRIDES: dict[str, str] = {
    # Shared responsibility & concepts — route by topic
    "shared-responsibility-ec2-001": COMPUTE,
    "shared-responsibility-ec2-customer-001": COMPUTE,
    "shared-responsibility-customer-two-001": SECURITY,
    "shared-responsibility-healthcare-rds-s3-data-001": SECURITY,
    "shared-responsibility-aws-physical-security-001": SECURITY,
    "shared-responsibility-managed-database-customer-001": DATABASES,
    # Trusted Advisor spans domains; keep under Monitoring per exam “operations”
    "trusted-advisor-not-area-001": MON_GOV,
    "trusted-advisor-unused-ec2-001": MON_GOV,
    "trusted-advisor-idle-cost-001": MON_GOV,
    "trusted-advisor-cost-security-best-practices-001": MON_GOV,
    "trusted-advisor-continuous-evaluation-suggestions-001": MON_GOV,
    # Elastic Beanstalk doc → self-support / Pricing & Support
    "elastic-beanstalk-documentation-001": PRICING,
    # APN / Professional Services
    "apn-consulting-no-aws-expert-001": PRICING,
    "professional-services-paid-adoption-001": PRICING,
    "professional-services-security-audit-best-practices-001": PRICING,
    # Global vs HA
    "ha-multi-az-001": GOING_GLOBAL,
    "global-services-choose-two-001": GOING_GLOBAL,
    # QuickSight → analytics
    "quicksight-dashboards-001": AI_ML_DATA,
    # ACM cert renewal could be security; user module list uses long Security section
    "acm-cert-renewal-001": SECURITY,
    "acm-data-in-transit-tax-001": SECURITY,
    # Service Catalog → governance
    "service-catalog-multitier-001": MON_GOV,
    # Cost in name but tool is explorer
    "cost-explorer-forecast-spend-001": PRICING,
    # S3 + IAM role — identity
    "ec2-s3-access-iam-role-001": SECURITY,
    # Architecture patterns
    "loosely-coupled-failure-001": WELL_ARCH,
    "tightly-vs-loosely-coupled-001": WELL_ARCH,
    # IaaS / cloud economics — foundational, map to Pricing & Support (cost model)
    "iaas-benefits-001": PRICING,
    "cloud-efficient-resources-001": GOING_GLOBAL,
    "cloud-innovation-focus-001": GOING_GLOBAL,
    "cloud-compute-provisioning-001": COMPUTE,
    "cloud-concept-reusability-001": WELL_ARCH,
    # AWS Marketplace vendor
    "aws-marketplace-vendor-001": PRICING,
    "marketplace-saas-datasets-select-two-001": PRICING,
    "marketplace-compliance-ready-regulations-001": PRICING,
    # Console / CLI — compute ops tooling
    "aws-gui-console-001": COMPUTE,
    "aws-cli-advantage-vs-console-001": COMPUTE,
    # Bug tracking SNS — messaging / app integration → compute track
    "bug-tracking-sns-notify-001": COMPUTE,
    "sns-topic-subscribers-001": MON_GOV,
    # Personal Health Dashboard
    "personal-health-dashboard-maintenance-001": MON_GOV,
    # DMS
    "dms-problem-minimize-migration-downtime-001": MIGRATING,
    "dms-managed-database-plan-assess-convert-migrate-001": MIGRATING,
    "dynamodb-staffing-unpredictable-traffic-autoscaling-001": DATABASES,
    # Backup service
    "aws-backup-main-problem-fragmented-001": STORAGE,
    "aws-backup-feature-centralized-policies-001": STORAGE,
    # Kinesis / analytics
    "kinesis-financial-real-time-stock-data-001": AI_ML_DATA,
    "data-pipeline-ingestion-kinesis-firehose-001": AI_ML_DATA,
    # X-Ray — observability
    "x-ray-debugging-performance-marketing-001": MON_GOV,
    # “Other” application services at end of file
    "amazon-connect-customer-service-ai-001": AI_ML_DATA,
    "appstream-saas-access-employees-001": COMPUTE,
    "workspaces-remote-workforce-secure-001": COMPUTE,
    "iot-solutions-examples-select-two-001": AI_ML_DATA,
    "iot-core-manufacturing-equipment-monitoring-001": AI_ML_DATA,
    "amplify-fullstack-auth-storage-001": COMPUTE,
    "ses-marketing-emails-engagement-001": PRICING,
    # Cost / rightsize — WA cost pillar
    "ec2-rightsize-low-cpu-usage-001": WELL_ARCH,
    "cost-drivers-healthcare-cto-001": PRICING,
    "edtech-cost-drivers-fundamental-select-three-001": PRICING,
}


def module_for(qid: str) -> str:
    if qid in OVERRIDES:
        return OVERRIDES[qid]

    if qid.startswith("well-architected"):
        return WELL_ARCH

    if (
        qid.startswith("migration-")
        or qid.startswith("caf-")
        or qid.startswith("datasync-")
        or qid.startswith("transfer-family-")
        or qid.startswith("mgn-")
        or qid.startswith("sct-")
        or "seven-rs-migration" in qid
    ):
        return MIGRATING

    if qid.startswith(("billing-", "aws-budget-", "budgets-", "pricing-calculator-", "enterprise-support-", "self-support-")):
        return PRICING

    if qid.startswith("cost-") and "healthcare" not in qid:
        return PRICING

    if qid.startswith(("marketplace-", "aws-partner-", "apn-role-", "aws-payment-")):
        return PRICING

    if qid.startswith(("cloudwatch-", "cloudtrail-", "artifact", "config-", "organizations-", "control-tower-", "license-manager-", "service-catalog-", "customer-compliance-center")):
        return MON_GOV

    if qid.startswith("trusted-advisor") or qid.startswith("scp-organizations"):
        return MON_GOV

    if qid.startswith(("iam-", "mfa-", "secrets-manager", "cloudhsm-", "encryption-", "ddos-", "boutique-ddos", "authz-", "authentication-", "least-privilege", "aws-shield", "kms-", "inspector-", "detective-", "security-hub", "encrypt-data-in-transit", "root-user-protect")):
        return SECURITY

    if qid.startswith(("comprehend-", "lex-", "polly-", "sagemaker-", "bedrock-", "generative-", "ml-", "foundation-models", "q-developer-", "q-business-", "personalize-", "classical-programming-vs", "amazon-connect-", "data-analytics-", "data-pipeline", "data-visualization", "glue-data-", "s3-unstructured-data-pipeline", "s3-redshift-data-lake", "etl-data-", "bedrock-foundation")):
        return AI_ML_DATA

    if qid.startswith(("rds-", "dynamodb-", "aurora-", "elasticache-", "neptune-", "documentdb-", "nosql-vs-relational", "ec2-unmanaged-database", "in-memory-caching-definition")):
        return DATABASES

    if qid.startswith(("s3-", "ebs-", "efs-", "fsx-", "storage-gateway", "dlm-", "elastic-disaster-recovery", "storage-glacier")) or "instance-store" in qid:
        return STORAGE

    if qid.startswith(("vpc-", "route53-", "direct-connect", "network-acl-", "cloudfront-", "global-accelerator-", "elb-", "ec2-public-subnet-security-groups", "security-group-change")):
        return NETWORKING

    if qid.startswith(("region-", "edge-", "nonprofit-region-", "global-infrastructure", "multi-region-", "regions-az-", "az-advantage", "ec2-multiple-az-reason", "fluctuating-demand-auto-scale", "scalability-elasticity", "iac-cloudformation-multi-region", "cloudformation-", "edge-locations-")):
        return GOING_GLOBAL

    if qid.startswith(
        (
            "ec2-",
            "lambda-",
            "ami-",
            "autoscaling-",
            "patch-os-",
            "nodejs-elastic-",
            "managed-vs-ec2-",
            "elastic-beanstalk-",
            "containers-",
            "container-",
            "compute-services-",
            "aws-batch-",
            "outposts-",
            "serverless-",
            "ecr-",
            "lightsail-",
            "spot-",
            "on-demand-",
            "dedicated-hosts-",
            "predictable-workloads-",
            "savings-plans-",
            "general-purpose-ec2-",
            "compute-optimized-",
            "sqs-",
            "elastic-beanstalk-startup",
        )
    ):
        return COMPUTE

    if qid.startswith(("workspaces-", "loosely-coupled", "tightly-vs-loosely")):
        return COMPUTE if "workspaces" in qid else WELL_ARCH

    # Default: try keyword heuristics
    lower = qid.lower()
    if any(k in lower for k in ("pricing", "support", "budget", "billing", "cost-explorer", "marketplace", "partner")):
        return PRICING
    if any(k in lower for k in (" vpc", "subnet", "route53", "cloudfront", "load-bal", "direct-connect", "nacl", "security-group", "global-accelerator")):
        return NETWORKING

    return GOING_GLOBAL


def main() -> None:
    root = Path(__file__).resolve().parents[1]
    path = root / "app" / "src" / "main" / "assets" / "questions.json"
    data = json.loads(path.read_text())
    seen = set()
    for q in data["questions"]:
        qid = q["id"]
        mod = module_for(qid)
        q["module"] = mod
        seen.add(mod)

    path.write_text(json.dumps(data, indent=2, ensure_ascii=False) + "\n")
    print("Updated", path)
    print("Modules used:", sorted(seen))


if __name__ == "__main__":
    main()
