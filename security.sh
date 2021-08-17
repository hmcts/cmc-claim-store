#!/usr/bin/env bash
export TEST_URL=http://cmc-claim-store-aat.service.core-compute-aat.internal
echo ${TEST_URL}
zap-api-scan.py -t ${TEST_URL}/v2/api-docs -f openapi -S -d -u ${SecurityRules} -P 1001 -l FAIL -J report.json -r api-report.html
mkdir -p functional-output
chmod a+wx functional-output
cp /zap/api-report.html functional-output/
