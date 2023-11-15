# **Active directory resource mapping**
This configuration example contains midpoint objects designed to work with the active directory as a source and destination resource based on the First Steps methodology (see documentation https://docs.evolveum.com/midpoint/methodology/first-steps/), which is also presented in the MidPoint Deployment: First Steps [MID301] training
### Prerequisities
1. running midpoint server with 4.8 version or later
2. available Active directory server certificates and imported into truststore
3. account in Active directory with domain admin privileges, used for midpoint connector configuration

### Deployment instructions

1. get Active Directory server certificate from SSL/TLS connection and import it into truststore, for detailed instruction how to setup TLS/SSL communication, please see related documentation page https://docs.evolveum.com/midpoint/reference/support-4.8/security/crypto/ssl-connections-client-side-/
2. copy all subfolders into midpoint studio and upload objects, for detailed instruction how to work with midpoint studio,please see related documentation page https://docs.evolveum.com/midpoint/tools/studio/usage/.
3. assign all authorization roles with prefix "IAM - System-Access -" to user. In this case midpoint objects are configured using authorization roles,  please see related documentation page  https://docs.evolveum.com/midpoint/reference/support-4.8/security/authorization/).
4. set Basic resource configuration attributes according to your Active Directory deployment using resource wizard, please see related documentation page https://docs.evolveum.com/midpoint/reference/support-4.8/admin-gui/resource-wizard/
5. configuration was set to use in development mode to avoid unwanted modification on Active directory objects, how midpoint works in simulation mode, please refer to documentation https://docs.evolveum.com/midpoint/reference/support-4.8/simulation/
