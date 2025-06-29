CA_DIR=CA-RSA
mkdir -p $CA_DIR/{private,newcerts,crl}
touch $CA_DIR/index.txt
echo 1000 > $CA_DIR/serial
sed "/^dir/cdir=$CA_DIR" openssl.cnf > $CA_DIR/openssl.cnf
openssl genpkey -algorithm RSA -out $CA_DIR/private/ca.key -aes256 -pkeyopt rsa_keygen_bits:2048
openssl req -x509 -new -sha256 -key $CA_DIR/private/ca.key -out $CA_DIR/ca.crt -days 3650 \
    -subj "/C=CN/ST=GuangDong/L=DongGuang/O=Gsign/CN=rootCA-RSA"
	
	
openssl genpkey -algorithm RSA -out server-rsa.key -pkeyopt rsa_keygen_bits:2048
openssl req -new -sha256 -key server-rsa.key -out server-rsa.csr \
    -subj "/C=CN/ST=GuangDong/L=DongGuang/O=Gsign/CN=server-rsa"
	
openssl ca -config $CA_DIR/openssl.cnf -in server-rsa.csr -out server-rsa.crt -days 365 \
    -cert $CA_DIR/ca.crt -keyfile $CA_DIR/private/ca.key -notext -md sha256
	
openssl pkcs12 -export -in server-rsa.crt -inkey server-rsa.key \
    -name server-rsa -out server-rsa.p12 -password pass:123456
	

openssl genpkey -algorithm RSA -out client-rsa.key -pkeyopt rsa_keygen_bits:2048
openssl req -new -sha256 -key client-rsa.key -out client-rsa.csr \
    -subj "/C=CN/ST=GuangDong/L=DongGuang/O=Gsign/CN=client-rsa"
	
openssl ca -config $CA_DIR/openssl.cnf -in client-rsa.csr -out client-rsa.crt -days 365 \
    -cert $CA_DIR/ca.crt -keyfile $CA_DIR/private/ca.key -notext -md sha256



# 创建域名私钥
openssl genrsa -out services.gradle.org.key 2048

# 生成 CSR（证书签名请求）
openssl req -new -key services.gradle.org.key -out services.gradle.org.csr \
  -subj "/CN=services.gradle.org" \
  -addext "subjectAltName=DNS:services.gradle.org,DNS:services.gradle.net"

cat > $CA_DIR/services.gradle.ext <<EOF
authorityKeyIdentifier=keyid,issuer
basicConstraints=CA:FALSE
keyUsage=digitalSignature,keyEncipherment
extendedKeyUsage=serverAuth
subjectAltName=DNS:services.gradle.org,DNS:services.gradle.net
EOF

openssl ca -config $CA_DIR/openssl.cnf -extfile $CA_DIR/services.gradle.ext -in services.gradle.org.csr -out services.gradle.org.crt -days 365 \
    -cert $CA_DIR/ca.crt -keyfile $CA_DIR/private/ca.key -notext -md sha256