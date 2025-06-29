CA_DIR=CA-ECC01
# 创建域名私钥
openssl genrsa -out server.jnetool.org.key 2048

# 生成 CSR（证书签名请求）
openssl req -new -key server.jnetool.org.key -out server.jnetool.org.csr \
  -subj "/CN=server.jnetool.org" \
  -addext "subjectAltName=DNS:server3.jnetool.org,DNS:server4.jnetool.org"

cat > $CA_DIR/server.jnetool.org.ext <<EOF
authorityKeyIdentifier=keyid,issuer
basicConstraints=CA:FALSE
keyUsage=digitalSignature,keyEncipherment
extendedKeyUsage=serverAuth
subjectAltName=DNS:server3.jnetool.org,DNS:server4.jnetool.org
EOF

openssl ca -config $CA_DIR/openssl.cnf -extfile $CA_DIR/server.jnetool.org.ext -in server.jnetool.org.csr -out server.jnetool.org.crt -days 365 \
    -cert $CA_DIR/ca.crt -keyfile $CA_DIR/private/ca.key -notext -md sha256
