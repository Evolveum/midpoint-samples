# **Active directory simple resource mapping**
<!-- TOC -->
* [Prerequisities](#prerequisities)
* [Deployment instructions](#deployment-instructions)
<!-- TOC -->
This configuration example contains midpoint objects designed to work with the active directory as a source and destination resource based on the First Steps methodology (see documentation https://docs.evolveum.com/midpoint/methodology/first-steps/), which is also presented in the MidPoint Deployment: First Steps [MID301] training.
### Prerequisities
1. running midpoint server with 4.8 version or later
2. available Active directory server certificates
3. account in Active directory with domain admin privileges, used for midpoint connector configuration

### Deployment instructions

1. get Active Directory server certificate from SSL/TLS connection and import it into truststore, for detailed instruction how to setup TLS/SSL communication, please see related documentation page https://docs.evolveum.com/midpoint/reference/support-4.8/security/crypto/ssl-connections-client-side-/
2. import objects into midPoint. Three alternative ways how to upload all related mP configuration objects:
* use script file upload.sh from linux console. Run script with url parameter specified for midpoint application like:

    `./upload.sh -<midpointUrl>`

    if no parameter is set default url will be used (default midpointUrl value http://localhost:8080).
* or upload into midpoint using GUI. Click on left side menu item "Import object", then choose file and import it. Repeat steps for all midpoint objects in this sample.
* or copy all subfolders into midpoint studio and upload objects, for detailed instruction how to work with midpoint studio,please see related documentation page https://docs.evolveum.com/midpoint/tools/studio/usage/#uploading-midpoint-objects. Please import midpoint objects in order
   * resource configuration xml file from folder resources
     * ADfirststep.xml
  * run task for test resource
      * 10-test-all-resources.xml
3. set Basic resource configuration attributes according to your Active Directory deployment using resource wizard, how to work with resource wizard, please see related documentation page https://docs.evolveum.com/midpoint/reference/support-4.8/admin-gui/resource-wizard/
4. configuration was set to use in development mode to avoid unwanted modification on Active directory objects, how midpoint works in simulation mode, please refer to documentation https://docs.evolveum.com/midpoint/reference/support-4.8/simulation/
