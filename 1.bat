call mvn clean install -Dmaven.test.skip=true

cp core\jbpm-gui\target\aperteworkflow.war C:\work\axa\liferay\deploy\aperteworkflow.war

rm -rf C:\work\axa\liferay\tomcat-6.0.29\webapps\aperteworkflow
rm -rf C:\work\axa\liferay\felix-cache
rm -rf C:\work\axa\liferay\lucene-index
rm -rf C:\work\axa\liferay\tomcat-6.0.29\work
rm -rf C:\work\axa\liferay\tomcat-6.0.29\temp

FOR /R c:\Users\Piotrek\Documents\GitHub\aperte-workflow-core\plugins %x IN (target\*.jar) DO echo W|copy %x c:\work\axa\liferay\osgi-plugins
FOR /R c:\work\axa\axa_esod_plugins_2.0 %x IN (target\*.jar) DO echo W|copy %x c:\work\axa\liferay\osgi-plugins
