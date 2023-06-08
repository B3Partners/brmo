set -euo pipefail

openssl req -new -text -passout pass:abcd -subj /CN=brmo-db -out brmo-db.csr -keyout tempkey.pem
openssl rsa -in tempkey.pem -passin pass:abcd -out private/private.key
openssl req -x509 -in brmo-db.csr -text -key private/private.key -days 365 -out certs/certificate.pem
rm brmo-db.csr tempkey.pem
