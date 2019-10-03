===========================================
Welcome to OptaWeb Employee Rostering
===========================================

CONTENT:
A. Running the web application on OpenShift
B. Running the web application locally

-------------------------------------------
A. Running the web application on OpenShift
-------------------------------------------

After logging in to your OpenShift instance, do:

cd sources
./provision.sh setup employee-rostering --binary

-------------------------------------------
B. Running the web application locally
-------------------------------------------

1. Copy the .war file (optaweb-employee-rostering-webapp-VERSION.war)
   that is available in the binaries directory to the
   $EAP_HOME/standalone/deployments directory.

2. In the $EAP_HOME/bin directory, run
   ./standalone.sh

3. In your web browser, open http://localhost:8080/.
