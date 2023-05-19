# Samples from Baptist School of Health Professions

These samples are a few, trimmed down/simplified versions, of our [midPoint identity management system](https://evolveum.com/midpoint/).
The objects were exported using the Eclipse plugin for midPoint and maintains the same folder structure.

## Scenario

Active Directory and OpenDJ synchronization of users, groups, organizational units (inbound and outbound).
There are some little unique things, the way it created the AD Groups in midPoint,
some groups are roles that are prefixed with 'Domain Group:' and then course specific
groups are imported as subtype, both with createOnDemand.

Most of the assignments are automatic using rules and policies.
If you take a look at the [Metarole_ Affiliations Policy.xml](https://github.com/JasonEverling/midpoint-samples/blob/master/samples/contrib/bshp/objects/roles/Metarole_%20Affiliations%20Policy.xml)
it simply gets assigned to each 'Primary Affiliations' role and ensure that each object can only ever have '1' Primary assigned.
It will remove/prune the previous role when a new one is applied.
The policy is using a `policyConstraint` excluding itself which creates the 'radio button' type role in a simple way.

There are some lookupTables for roles, orgs, and services, unique definitions for our usage
but it shows what a lookupTable can be used for and how to apply them in the `objectTemplates`.

We also have examples using the `includeRef` feature in midPoint to reduce the size of our template.
A unique example is our use of auto archiving attributes that are changed, that need to be retained, such as last name and username.
When a `username` or `lastname` is changed, midPoint will populate a custom schema extension field, `previousLastname` and `previousUsername`.
These attributes are set to read-only in the objectTemplate so they can only be read by a human but changed by the system.
The archiving of the `username` attribute allows us to ensure that a new username,
if it does get changes which is rare for us, doesn't ever get assigned again.

There are much more examples and usages to explore in these samples, give them a look see.
