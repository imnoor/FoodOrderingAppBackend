"# FoodOrderingAppBackend" 

to run: 

first clone this project from github url.

step 1:
create a db 'restaurantdb' in your  local  postgres 
put the right password in localhost.properties in FoodOrderingApp-db folder

step 2:
run below in terminal at the cloned folder.
mvn clean install -Psetup -DskipTests

step 3: 
do "mvn clean install"

step 4:
run the application in intellij

now hit localhost:8080/api/swagger-ui.html