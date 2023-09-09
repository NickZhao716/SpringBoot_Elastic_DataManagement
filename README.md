# SpringBoot_Elastic_DataManagement
System structure:
![image](https://github.com/NickZhao716/SpringBoot_Elastic_DataManagement/assets/104879437/b522e6b2-dcae-47be-a7c2-291fa3b0cd5a)
System requirment:
Virtual Machine Manager(VMM) such as Oracle Virtual box.
- Redis cluster engine.
- Kafka engine.
- Elastic Search engine.

Noted, all the external service config can be modified under project config folder.
![image](https://github.com/NickZhao716/SpringBoot_Elastic_DataManagement/assets/104879437/575722a3-859c-4dea-9d6d-81949af69e29)

1. Provied CRUD API for data management. Support Patch operation.
   example:
   ![image](https://github.com/NickZhao716/SpringBoot_Elastic_DataManagement/assets/104879437/fcb5ab24-834d-4ad3-b766-4629d66bb64a)
2. All the CRUD operations require Token.
   
   Request Google OAuth2.0 JWT before make and CRUD request.
   
   example:
   ![image](https://github.com/NickZhao716/SpringBoot_Elastic_DataManagement/assets/104879437/ff6b129f-87cc-47ae-82fe-e8ffbd48ae91)
   
   noted, Google OAuth service required registration.
3. Ether PostMan or ElasticSeach client can be used for data Querying.
   
   Query scripts is under Testing-ElasticSearchQueries.txt.
   
