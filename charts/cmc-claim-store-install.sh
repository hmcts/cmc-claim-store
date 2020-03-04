#!/bin/sh

helm upgrade cmc-claim-store-pr-1474 cmc-claim-store \
  -f cmc-claim-store/values.yaml -f cmc-claim-store/values.preview.yaml \
  --install --wait --timeout 500s \
  --namespace money-claims \
  --set global.subscriptionId=1c4f0704-a29e-403d-b719-b9
