# Description

It's time to plan the architecture of our service. A good plan is 50% of the result. To begin with, we will determine the functions of our service, group them, and plan the appropriate endpoints following the principles of the REST API:

- Authentication
1. POST api/auth/signup allows the user to register on the service;
2. POST api/auth/changepass changes a user password.

- Business functionality
1. GET api/empl/payment gives access to the employee's payrolls;
2. POST api/acct/payments uploads payrolls;
3. PUT api/acct/payments updates payment information.

- Service functionality
1. PUT api/admin/user/role changes user roles;
2. DELETE api/admin/user deletes a user;
3. GET api/admin/user displays information about all users.
4. PUT api/admin/user/access locks / unlocks user

- Security Auditor
1. GET api/security/events gets all the events


To ensure the security of our service, we will also plan the distribution of roles:

|                          | Anonymous | User | Accountant | Administrator | Auditor |
|:-------------------------|:---------:|:----:|:----------:|:-------------:|:-------:|
| POST api/auth/signup     |     +     |  +   |     +      |       +       |    +    |
| POST api/auth/changepass |           |  +   |     +      |       +       |    -    |
| GET api/empl/payment     |     -     |  +   |     +      |       -       |    -    |
| POST api/acct/payments   |     -     |  -   |     +      |       -       |    -    |
| PUT api/acct/payments    |     -     |  -   |     +      |       -       |    -    |
| GET api/admin/user       |     -     |  -   |     -      |       +       |    -    |
| DELETE api/admin/user    |     -     |  -   |     -      |       +       |    -    |
| PUT api/admin/user/role  |     -     |  -   |     -      |       +       |    -    |
| PUT api/admin/user/access|     -     |  -   |     -      |       +       |    -    |
| GET api/security/events  |     -     |  -   |     -      |       -       |    +    |


The security department has put forward new requirements. The service must log information security events. Take a look at what they include:

| Description   | Event Name  |
|:-------------|:-----------|
|A user has been successfully registered	| CREATE_USER
|A user has changed the password successfully	|CHANGE_PASSWORD
|A user is trying to access a resource without access rights	|ACCESS_DENIED
|Failed authentication	|LOGIN_FAILED
|A role is granted to a user	|GRANT_ROLE
|A role has been revoked	|REMOVE_ROLE
|The Administrator has locked the user	|LOCK_USER
|The Administrator has unlocked a user	|UNLOCK_USER
|The Administrator has deleted a user	|DELETE_USER
|A user has been blocked on suspicion of a brute force attack	|BRUTE_FORCE

The composition of the security event fields is presented below:

```
{
    "date": "<date>",
    "action": "<event_name from table>",
    "subject": "<The user who performed the action>",
    "object": "<The object on which the action was performed>",
    "path": "<api>"
}
```

If it is impossible to determine a user, output Anonymous in the subject field. All examples of events are provided in the Examples.

Also, you need to add the role of the auditor. The auditor is an employee of the security department who analyzes information security events and identifies incidents. You need to add the appropriate endpoint for this. A user with the auditor role should be able to receive all events using the endpoint. The auditor is a part of the business group. We suggest that you implement the storage of information security events in the database, although you can choose another solution. Make sure it is persistent.

Let's also discuss what a security incident is. For example, if a user made a mistake in entering a password. This is a minor user error, but numerous repeated attempts to log in with the wrong password can be evidence of a brute-force attack. In this case, it is necessary to register the incident and conduct an investigation. Information security events are collected in our service to serve as a basis for identifying incidents in the future after transmission to the Security Information and Event Management systems (SIEM).

Let's implement a simple rule for detecting a brute force attack. If there are more than 5 consecutive attempts to enter an incorrect password, an entry about this should appear in the security events. Also, the user account must be blocked.

To unlock a user, you will need to add a new administrative endpoint: api/admin/user/access.
