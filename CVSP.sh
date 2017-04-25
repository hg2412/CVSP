#!/bin/bash
export HOST_NAME=localhost
export PUBLIC_IP=YOUR_PUBLIC_IP
export JAVA_HOME="/usr/lib/jvm/java-8-openjdk-amd64"
export HADOOP_HOME="/usr/local/hadoop"
export PATH="$PATH:$HBASE_HOME/bin"
export PATH="$PATH:$HADOOP_HOME/bin"
export HADOOP_PREFIX="/usr/local/hadoop"



#install apache server
#! /bin/bash
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
wget https://github.com/sequenceiq/docker-hadoop-ubuntu/archive/master.zip
unzip master.zip -d /master
sed -i '/^export JAVA_HOME/ s:.*:export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64\nexport HADOOP_PREFIX=/usr/local/hadoop\nexport HADOOP_HOME=/usr/local/hadoop\n:' $HADOOP_PREFIX/etc/hadoop/hadoop-env.sh
export CONFIG_HOME=/master/docker-hadoop-ubuntu-master/
cp $CONFIG_HOME/core-site.xml.template $HADOOP_PREFIX/etc/hadoop/core-site.xml.template
sed s/HOSTNAME/$PUBLIC_IP/ /usr/local/hadoop/etc/hadoop/core-site.xml.template > /usr/local/hadoop/etc/hadoop/core-site.xml
cp $CONFIG_HOME/hdfs-site.xml $HADOOP_PREFIX/etc/hadoop/hdfs-site.xml
cp $CONFIG_HOME/mapred-site.xml $HADOOP_PREFIX/etc/hadoop/mapred-site.xml
cp $CONFIG_HOME/yarn-site.xml $HADOOP_PREFIX/etc/hadoop/yarn-site.xml

echo -e "<property>\n  <name>yarn.app.mapreduce.am.staging-dir</name>\n  <value>/user</value>\n</property>
"
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

