#!/bin/bash
export HOST_NAME=localhost
export PUBLIC_IP=YOUR_PUBLIC_IP
export JAVA_HOME="/usr/lib/jvm/java-8-openjdk-amd64"
export HADOOP_HOME="/usr/local/hadoop"
export PATH="$PATH:$HBASE_HOME/bin"
export PATH="$PATH:$HADOOP_HOME/bin"
export HADOOP_PREFIX="/usr/local/hadoop"


#install apache server
apt-get update
apt-get install -y shellinabox
invoke-rc.d shellinabox restart
useradd guest1
echo -e "123456\n123456\n" | passwd guest1
usermod -aG sudo guest1
apt-get install -y apache2
cat <<EOF > /var/www/html/index.html
<html><body><h1>Cloud Virtual Service Provider</h1>
<p>Your username is guest1, password is 123456</p>
<p>You can run your jobs using the terminal:</p>
<p><a href="https://YOUR_PUBLIC_IP:4200">https://YOUR_PUBLIC_IP:4200</a></p>
<p>You can check your mapreduce jobs:</p>
<p><a href="http://YOUR_PUBLIC_IP:8088">http://YOUR_PUBLIC_IP:8088</a></p>
</body></html>
EOF


apt-get update
apt-get install -y maven
apt-get install -y awscli
apt-get install -y ruby
apt-get install default-jre -y
apt-get install default-jdk -y
apt-get install unzip

#install hadoop
wget http://apache.mirrors.tds.net/hadoop/common/hadoop-2.7.3/hadoop-2.7.3.tar.gz
tar -xf hadoop-2.7.3.tar.gz
rm hadoop-2.7.3.tar.gz
mv hadoop-2.7.3 /usr/local/hadoop

#config hadoop
rm -rf /usr/local/hadoop/etc/hadoop
gsutil cp -r gs://cvsp/hadoop-config/hadoop /usr/local/hadoop/etc/
export PUBLIC_IP=$(curl icanhazip.com)
# change public ip in config files
find /usr/local/hadoop/etc/hadoop/ -type f -exec sed -i "s/YOUR_PUBLIC_IP/$PUBLIC_IP/g" {} +


# setup gsutil connector
wget https://storage.googleapis.com/hadoop-lib/gcs/gcs-connector-latest-hadoop2.jar
cp gcs-connector-latest-hadoop2.jar $HADOOP_PREFIX/share/gcs-connector-latest-hadoop2.jar
cp gcs-connector-latest-hadoop2.jar $HADOOP_PREFIX/share/hadoop/yarn/lib/gcs-connector-latest-hadoop2.jar


apt-get install ssh
apt-get install rsync

rm -f ~/.ssh/*
ssh-keygen -t rsa -P '' -f ~/.ssh/id_rsa
cat ~/.ssh/id_rsa.pub >> ~/.ssh/authorized_keys
chmod 0600 ~/.ssh/authorized_keys
echo -e "Host *\n  StrictHostKeyChecking no" > ~/.ssh/config
chmod 400 ~/.ssh/config

#start hdfs
$HADOOP_PREFIX/bin/hdfs namenode -format
sleep 5
$HADOOP_PREFIX/sbin/start-dfs.sh
sleep 2
$HADOOP_PREFIX/bin/hdfs dfs -mkdir -p /user/root
sleep 2
$HADOOP_PREFIX/bin/hdfs dfs -put $HADOOP_PREFIX/etc/hadoop/ input

#start YARN
$HADOOP_PREFIX/sbin/start-yarn.sh
sleep 10
hadoop jar $HADOOP_PREFIX/share/hadoop/mapreduce/hadoop-mapreduce-examples-2.7.3.jar wordcount input output

bin/hadoop jar $HADOOP_PREFIX/share/hadoop/mapreduce/hadoop-mapreduce-examples-2.7.3.jar wordcount gs://cvsp/randomtexts/part-m-00000 gs://cvsp/output-$PUBLIC_IP