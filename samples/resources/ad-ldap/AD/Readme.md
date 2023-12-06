# **Active directory resource mapping**
<!-- TOC -->
* [Prerequisities](#prerequisities)
* [Deployment instructions](#deployment-instructions)
<!-- TOC -->
This configuration example contains midpoint objects designed to work with the active directory as a source and destination resource based on the First Steps methodology (see documentation https://docs.evolveum.com/midpoint/methodology/first-steps/), which is also presented in the MidPoint Deployment: First Steps [MID301] training
### Prerequisities
1. running midpoint server with 4.8 version or later
2. available Active directory server certificates
3. account in Active directory with domain admin privileges, used for midpoint connector configuration

### Deployment instructions

1. get Active Directory server certificate from SSL/TLS connection and import it into truststore, for detailed instruction how to setup TLS/SSL communication, please see related documentation page https://docs.evolveum.com/midpoint/reference/support-4.8/security/crypto/ssl-connections-client-side-/
2. import objects into midPoint.
   Two alternatives follow:
* all related mP configuration objects could be uploaded into midpoint using GUI. Click on left side menu item "Import object", then choose file and import it. Repeat steps for all midpoint objects in this sample.
* copy all subfolders into midpoint studio and upload objects, for detailed instruction how to work with midpoint studio,please see related documentation page https://docs.evolveum.com/midpoint/tools/studio/usage/#uploading-midpoint-objects. Please import midpoint objects in order
    * resource configuration xml file from folder lookuptables
      * adGroupTypes.xml
    * resource configuration xml file from folder resources
      * ADfirststep.xml
    * all archetypes xml files from folder archetypes
     * ADGroupDomainLocalDistributionGroup.xml
     * ADGroupDomainLocalSecurityGroup.xml
     * ADGroupGlobalDistributionGroup.xml
     * ADGroupGlobalSecurityGroup.xml
     * ADGroupRoot.xml
     * ADGroupUniversalDistributionGroup.xml
     * ADGroupUniversalSecurityGroup.xml
     * ADManagedServiceAccount.xml
     * ADUserAccount.xml
     * IAMSystemAccessRole.xml
   * all roles from folder roles
     * Authorization-System-Access-ActiveDirectoryAccount.xml
     * Authorization-System-Access-ActiveDirectoryGroups.xml
     * Authorization-System-Access-ActiveDirectoryMSA.xml

4. assign all authorization roles with prefix "IAM - System-Access -" to user. In this case midpoint objects are configured using authorization roles, for more information how it works you could find in related documentation page  https://docs.evolveum.com/midpoint/reference/support-4.8/security/authorization/).
   * In your browser with midPoint, edit user and assign all of the imported roles by following steps:
     * go to Assignments
     * click New assignment button
     * select Role tab
     * select all roles with prefix "Authorization-System-Access-"
     * make sure “Relation” in Parameters section is set to Default (this is the default)
     * click Add button
     * click Save button
7. set Basic resource configuration attributes according to your Active Directory deployment using resource wizard, how to work with resource wizard, please see related documentation page https://docs.evolveum.com/midpoint/reference/support-4.8/admin-gui/resource-wizard/
8. configuration was set to use in development mode to avoid unwanted modification on Active directory objects, how midpoint works in simulation mode, please refer to documentation https://docs.evolveum.com/midpoint/reference/support-4.8/simulation/
