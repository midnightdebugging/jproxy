#CA目录创建
CA_DIR=CA-ECC
openssl ecparam --list_curves | grep secp384r1

mkdir -p $CA_DIR/{private,newcerts,crl}
touch $CA_DIR/index.txt
echo 1000 > $CA_DIR/serial

sed "/^dir/cdir=$CA_DIR" openssl.cnf > $CA_DIR/openssl.cnf
#生成自签名证书
openssl ecparam -name secp384r1 -genkey -out $CA_DIR/private/ca.key

openssl req -new -x509 -key $CA_DIR/private/ca.key -out $CA_DIR/ca.crt -days 3650 \
-subj "/C=CN/ST=G_sign/L=G_sign/O=G_sign/CN=G_sign"

#生成用户证书server01
openssl ecparam -name secp384r1 -genkey -out server01.key

openssl req -new -key server01.key -out server01.csr \
    -subj "/C=CN/ST=G_sign/L=G_sign/O=G_sign/CN=server01"

openssl ca -config $CA_DIR/openssl.cnf -in server01.csr -out server01.crt -days 365 \
    -cert $CA_DIR/ca.crt -keyfile $CA_DIR/private/ca.key -notext 



#生成用户证书client01
openssl ecparam -name secp384r1 -genkey -out client01.key

openssl req -new -key client01.key -out client01.csr \
    -subj "/C=CN/ST=G_sign/L=G_sign/O=G_sign/CN=client01"

openssl ca -config $CA_DIR/openssl.cnf -in client01.csr -out client01.crt -days 365 \
    -cert $CA_DIR/ca.crt -keyfile $CA_DIR/private/ca.key -notext 


# 生成 PKCS12 文件（密码：123456）
openssl pkcs12 -export -in server01.crt -inkey server01.key \
    -name server -out server01.p12 -password pass:123456
openssl pkcs12 -export -in client01.crt -inkey client01.key \
    -name client01 -out client01.p12 -password pass:123456
#分析
openssl x509 -in client01.crt -text -noout | grep "Signature Algorithm"
openssl verify -CAfile $CA_DIR/ca.crt client01.crt