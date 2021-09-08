# Requirements Document 

Authors: Andrea Cavallo, Giulio Carota, Angelo Oscar Piccirillo, Matteo Biffoni

Date: April 19, 2021

Version: 1.0

# Contents

- [Essential description](#essential-description)
- [Stakeholders](#stakeholders)
- [Context Diagram and interfaces](#context-diagram-and-interfaces)
	+ [Context Diagram](#context-diagram)
	+ [Interfaces](#interfaces) 
	
- [Stories and personas](#stories-and-personas)
- [Functional and non functional requirements](#functional-and-non-functional-requirements)
	+ [Functional Requirements](#functional-requirements)
	+ [Non functional requirements](#non-functional-requirements)
- [Use case diagram and use cases](#use-case-diagram-and-use-cases)
	+ [Use case diagram](#use-case-diagram)
	+ [Use cases](#use-cases)
    	+ [Relevant scenarios](#relevant-scenarios)
- [Glossary](#glossary)
- [System design](#system-design)
- [Deployment diagram](#deployment-diagram)

# Essential description

Small shops require a simple application to support the owner or manager. A small shop (ex a food shop) occupies 50-200 square meters, sells 500-2000 different item types, has one or a few cash registers 
EZShop is a software application to:
* manage sales
* manage inventory
* manage customers
* support accounting


# Stakeholders


| Stakeholder name  | Subtype | Description | 
| ----------------- |:-------:|:-----------:|
| buyer | | customer who buys products |
| owner | | person who owns the shop (may work in the shop or not) |
| supplier | | person/company that supplies products to the shop
| employee | <ul><li>shop manager</li><li>cashier</li><li>accountant</li><li>inventory manager</li><li>shop assistant</li></ul> | people who work in the shop |
| IT admin | | person who maintains the software |
| Product | | product that is sold by the shop |

# Context Diagram and interfaces

## Context Diagram

```plantuml
	ShopManager --> (EzShop)
	Supplier --> (EzShop)
	ShopAssistant --> (EzShop)
	Cashier --|> ShopAssistant
	InventoryManager --|> ShopAssistant
	Accountant --|> ShopAssistant
	PaymentGateway --> (EzShop)
	Product --> (EzShop)
```

## Interfaces

| Actor | Logical Interface | Physical Interface  |
| ------------- |:-------------:| -----:|
|   User, Shop Manager  | Web GUI | Screen, keyboard on PC (or Cash Register) or smartphone |
|   Supplier  | Email | Internet |
|   PaymentGateway  | rest API version 2020-08-27 as in https://stripe.com/docs/api | Internet link |
|   Product  | Barcode | Barcode scanner |

# Stories and personas

### Persona: shop manager

- Maria is a 56 years old businesswoman who owns many shops of small size and administrates them as a shop manager. Since she is involved in a lot of different businesses, she needs to get some information fast and through simple steps. She is not directly interested in the dynamics of single shops, but she cares about the overall efficiency and revenue of her activities.

- Maurizio is 45 years old and he works as shop manager for a small shop. He likes being in direct contact with customers and employees and he takes care of the overall logistics of the shop, managing shifts and the inventory. He needs a software that provides a complete picture of the shop activity, from the overall view to smaller details.

### Persona: employee

- Nunzia is 62 years old and she lives in a small town with a supermarket, where she works as a shop assistant. She is not practical with computers, so she needs a simple software application to help her while doing her job.

- Jessica is 50 years old and she works as a cashier in a small shop on the other side of a big city with respect to her house. Since she has two children to bring to school and it takes 45 minutes for her to get to the shop from the school, she is only available for shifts that do not start too early in the morning.  

- Paolo is 40 years old and he works in a small supermarket as shop assistant and inventory manager. Since he has to assist a lot of customers, he needs a fast way to get information about the inventory, so that he can devote more time to customers. 


# Functional and non functional requirements

## Functional Requirements

| ID        | Description  |
| ------------- |:-------------:| 
|  FR1     | Manage Inventory|
|  FR1.1     | Modify product availability |
|  FR1.2     | List availabilities of products |
|  FR2     | Manage user account |
|  FR2.1     | Create user account |
|  FR2.2     | Delete user account |
|  FR2.3     | Modify user account |
|  FR3     | Manage accounting information |
|  FR3.1     | Modify accounting information  |
|  FR3.2     | Display accounting information |
|  FR3.3     | Filter accounting information |
|  FR4     | Manage sales |
|  FR4.1     | Insert a new transaction |
|  FR4.2     | Delete a transaction |
|  FR4.3     | Add a product to chart by barcode |
|  FR4.4     | Remove a product from chart by barcode |
|  FR4.5     | Close current sale and emit ticket |
|  FR5     | Manage fidelity cards |
|  FR5.1     | Create new fidelity card |
|  FR5.2     | Update credits on fidelity card |
|  FR5.3     | Delete fidelity card |
|  FR6     | Manage orders of products |
|  FR6.1     | Place an order |
|  FR6.2     | Cancel an order |
|  FR6.3     | List orders' status |
|  FR6.4     | Modify orders' status |
|  FR7     | Manage employees' shifts |
|  FR7.1     | Add employees' avalabilities |
|  FR7.2     | Assign Shifts |
|  FR8     | Manage employees' salaries |
|  FR8.1     | Update salaries |
|  FR8.2     | Manage overtime work |


## Access Rights for Users and Actors

| ID       |Admin / Shop Manager | Accountant | Cashier / Shop Assistant | Inventory Manager |
| -------- |:------------------:|:----------:|:------------------------:|:----------------:| 
|  FR1.1   | yes  | no  |no   | yes |
|  FR1.2   | yes  | no  | yes | yes |
|  FR2     | yes  | no  | no  | no 	|
|  FR3     | yes  | yes | no  | no	|
|  FR4.1   | yes  | yes | no  | no	|
|  FR4.2   | yes  | yes | no  | no	|
|  FR4.3   | yes  | no  | yes | no	|
|  FR4.4   | yes  | no  | yes | no	|
|  FR4.5   | yes  | no  | yes | no	|
|  FR5.1   | yes  | no  | yes | no	|
|  FR5.3   | yes  | no  | yes | no	|
|  FR6.1   | yes  | yes | no  | yes	|
|  FR6.2   | yes  | yes | no  | yes	|
|  FR6.3   | yes  | yes | yes | yes	|
|  FR6.3   | yes  | yes | no  | yes	|
|  FR7.1   | yes  | yes | no  | no	|
|  FR7.2   | yes  | yes | no  | no	|
|  FR8.1   | yes  | yes | no  | no	|
|  FR8.2   | yes  | yes | no  | no	|


## Non Functional Requirements

| ID        | Type          | Description  | Refers to |
| ------------- |:-------------:| :-----:| -----:|
|  NFR1     | Usability | Software should be used without specific training | All FR |
|  NFR2     | Performance | All operations should be performed in less than 0.5 sec | All FR |
|  NFR3     | Portability | Application should be accessible from smartphones and PCs. Compatibility with Chrome (version 81 and more recent), and Safari (version 13 and more recent) | All FR |
| NFR4 | Privacy | Account data should be protected | All FR |
| NFR5 | Security | User login should be executed securely | All FR | 


# Use case diagram and use cases


## Use case diagram


```plantuml

(Product sale) as UCSale
(Manage fidelity card) as UCCard
(Order products) as UCOrder
(Retrieve accounting information) as UCAccounting
(Retrieve inventory information) as UCInventory
(Manage shifts) as UCShifts
(Manage salaries) as UCSalaries

:Cashier: ---> UCSale
UCSale --> :Product:
UCSale ...> (Buy a product) : include
UCSale ....> (Return a product) : include
:Cashier: ---> UCCard
UCCard ....> (Create fidelity card) : include
UCCard ...> (Delete fidelity card) : include
UCCard ...> (Modify fidelity card) : include
UCOrder  --> :Supplier:
UCOrder --> :Product:
UCOrder ....> (Order of a product below a threshold) :include
UCOrder ....> (Order of a new product) :include
:Inventory manager: --> UCOrder
:Accountant: --> UCAccounting
:Shop manager: --> UCAccounting
:Cashier: ---> UCInventory
:Shop assistant: --> UCInventory
:Shop manager: --> UCInventory
:Inventory manager: --> UCInventory
UCInventory ...> (Check availability of a specific product) :include
UCInventory  ..> (Do complete inventory) :include
:Shop manager: ----> UCShifts
UCShifts ..> (Update shifts) :include
UCShifts ...> (Manage overtime work) :include
:Shop manager: ----> UCSalaries
UCSalaries ...> (Check salaries) :include
UCSalaries ..> (Update salaries) :include
```


### Use case 1, UC1 - Product sale
| Actors Involved        |  cashier, product |
| ------------- |:-------------:| 
|  Precondition     | <ul><li>availability of the product</li></ul> |  
|  Post condition     | <ul><li>ready for next sale</li></ul> |
|  Nominal Scenario     | <ul><li>customer buys one or more products</li></ul> |
|  Variants     |<ul><li>customer does not have enough money for the purchase</li><li>transaction in unsuccessful</li><li>customer wants to return a product</li></ul>|

##### Scenario 1.1

| Scenario 1.1 | Sale of a product |
| ------------- |:-------------:| 
|  Precondition     | <ul><li>Customer has enough money to buy the product</li></ul> |
|  Post condition     |<ul><li>customer has the product</li><li>remaining quantity of the product decreases</li><li>balance is incremented with transaction value</li></ul> |
| Step#        | Description  |
|  1     | customer shows up at the cash register with the product  |  
|  2     | cashier scans barcode with the scanner |
|  3     | customer pays |
|  4     | balance and availability of the product are updated |

##### Scenario 1.2

| Scenario 1.2 | Return of a product |
| ------------- |:-------------:| 
|  Precondition     |<ul><li>customer wants to return a product</li><li>customer has a fidelity card</li></ul> |
|  Post condition     |<ul><li>availabilities of the products are updated</li><li>balance is updated</li><li>customer has increased credit on her fidelity card</li></ul> |
| Step#        | Description  |
|  1     | customer shows up at the cash register with the old product  |  
|  2     | cashier scans barcode with the scanner |
| 3      | cashier scans the fidelity card of the customer | 
|  4    | customer gets credit on his fidelity card |
|  5     | balance and availability of the product are updated |


### Use case 2, UC2 - Order products

| Actors Involved        |  supplier, inventory manager, product |
| ------------- |:-------------:| 
|  Precondition     | <ul><li>the product availability is under his threshold</li><li>the inventory manager needs a new product</ul> |  
|  Post condition     | The order for the product is placed |
|  Nominal Scenario     | An order for a product is placed |
|  Variants     |<ul><li>The order is automatically managed by the system</li><li>the order is placed by the inventory manager</li></ul>|


##### Scenario 2.1

| Scenario 2.1 | Order of a product below a threshold |
| ------------- |:-------------:| 
|  Precondition     |<ul><li>product availability goes below a threshold</li><li>enough money to buy new products</li></ul>|
|  Post condition     |<ul><li>an order has been placed</li></ul> |
| Step#        | Description  |
|  1     | app detects product's low availability  |  
|  2     | app places an order for a predefined quantity of the product |
|  3     | when the supplier advices that the order has been accepted, the inventory manager inserts that the order has been placed |


##### Scenario 2.2

| Scenario 2.2 | Order of a new product |
| ------------- |:-------------:| 
|  Precondition     |<ul><li>inventory manager wants to buy a new product</li><li>enough money for the order</li></ul> |
|  Post condition     |<ul><li>an order has been placed</li></ul> |
| Step#        | Description  |
|  1     | inventory manager places an order for a new type of product |  
|  2     | when the supplier advices that the order has been accepted, the inventory manager inserts that the order has been placed |


### Use case 3, UC3 - Retrieve accounting information

| Actors Involved        |  accountant, shop manager |
| ------------- |:-------------:| 
|  Precondition     | <ul><li>information request</li></ul> |  
|  Post condition     |<ul><li>information are provided</li></ul>  |
|  Nominal Scenario     | accountant or shop manager get the information they want to access |
|  Variants     | - |


##### Scenario 3.1

| Scenario 3.1 | Request of information |
| ------------- |:-------------:| 
|  Precondition     | <ul><li>someone wants to get some information</li></ul>|
|  Post condition     |<ul><li>the request is satisfied</li></ul> |
| Step#        | Description  |
|  1     | someone places a request of information among the available statistics  |  
|  2     | the system calculates the requested information|
|  3      | the system provides the requested information  |


### Use case 4, UC4 - Manage fidelity card

| Actors Involved        |  cashier, shop manager |
| ------------- |:-------------:| 
|  Precondition     | <ul><li>a fidelity card has to be created/modified/removed</li></ul> |  
|  Post condition     |<ul><li>the fidelity card has been created/modified/removed</li></ul>  |
|  Nominal Scenario     | cashier create/modify/remove a fidelity card of a custumer |
|  Variants     | - |


##### Scenario 4.1

| Scenario 4.1 | creation of a fidelity card |
| ------------- |:-------------:| 
|  Precondition     | <ul><li>customer requires a fidelity card</li></ul>|
|  Post condition     | <ul><li>customer has the fidelity card</li></ul>|
| Step#        | Description  |
|  1     | cashier asks to the customer for his personal information (name, surname, etc.)  |  
|  2     | cashier emits the fidelity card |

##### Scenario 4.2
| Scenario 4.2 | cashier has to modify a fidelity card |
| ------------- |:-------------:| 
|  Precondition     | <ul><li>a fidelity card needs a modification</li></ul>|
|  Post condition     | <ul><li>the fidelity card has been modified</li></ul>|
| Step#        | Description  |
|  1     | cashier applys the modification to the part of the fidelity card that needs the changes  |  
|  2     | cashier saves the modification |

##### Scenario 4.3
| Scenario 4.2 | cancellation of a fidelity card |
| ------------- |:-------------:| 
|  Precondition     | <ul><li>a customer requires the cancellation of his fidelity card</li></ul>|
|  Post condition     | <ul><li>the fidelity card has been removed</li></ul>|
| Step#        | Description  |
|  1     | cashier searchs the fidelity card using its ID or by scanning it  |  
|  2     | cashier deletes the fidelity card |
|  3     | cashier confirms the cancellation |

### Use case 5, UC5 - Retrieve inventory information

| Actors Involved        |  cashier, inventory manager, shop assistant |
| ------------- |:-------------:| 
|  Precondition     | <ul><li>employee wants to get inventory information</li></ul> |  
|  Post condition     |<ul><li>employee gets inventory information</li></ul>  |
|  Nominal Scenario     | information about availabilities of the products is provided by the system |
|  Variants     | <ul><li>shop assistant requires information about availability of a single product</li><li>inventory manager requires information about availabilities of all products in the warehouse</li></ul> |


##### Scenario 5.1

| Scenario 5.1 | Shop assistant checks availability of a specific product |
| ------------- |:-------------:| 
|  Precondition     | <ul><li>customer requires information about product availabilty or shop assistant notices that specific products are not displayed anymore</li></ul>|
|  Post condition     | <ul><li>customer or shop assistant gets information about availability of the product</li></ul>|
| Step#        | Description  |
|  1     | shop assistant inserts desired product in the system  |  
|  2     | the system provides information about its availaibility in the warehouse |

##### Scenario 5.2

| Scenario 5.2 | Inventory manager does overall inventory |
| ------------- |:-------------:| 
|  Precondition     | <ul><li>inventory manager wants to do an overall inventory of the warehouse</li></ul>|
|  Post condition     | <ul><li>inventory manager has the information he needs</li></ul>|
| Step#        | Description  |
|  1     | inventory manager requires inventory information  |  
|  2     | the system provides information about availabilities in the warehouse |


### Use case 6, UC6 - Manage shifts

| Actors Involved        |  shop manager |
| ------------- |:-------------:| 
|  Precondition     | <ul><li>shifts of employees need to be changed or decided</li></ul> |  
|  Post condition   | <ul><li>shifts of employees are decided</li></ul>  |
|  Nominal Scenario     | Shop manager decides shifts of employees for the following week/month |
|  Variants     | <ul><li>employee needs to be substituted because she is sick or late</li><li>employee does overtime work</li></ul> |


##### Scenario 6.1

| Scenario 6.1 | Update employees' shifts |
| ------------- |:-------------:| 
|  Precondition     | <ul><li>a change in the shifts for the week is required (e.g. one employee is sick)</li></ul>|
|  Post condition     | <ul><li>shifts are up to date</li></ul>|
| Step#        | Description  |
|  1     | shop assistant inserts modification in the system  |  
|  2     | the system modifies the number of hours worked by an employee accordingly |

##### Scenario 6.2

| Scenario 6.2 | Manage overtime work |
| ------------- |:-------------:| 
|  Precondition     | <ul><li>employee works more than standard number of hours</li></ul>|
|  Post condition     | <ul><li>salary of employee is properly updated</li></ul>|
| Step#        | Description  |
|  1     | shop manager adds overtime work for an employee in the system  |  
|  2     | the salary of the employee for the current month is updated accordingly |



### Use case 7, UC7 - Manage salaries

| Actors Involved        |  shop manager |
| ------------- |:-------------:| 
|  Precondition     |  |  
|  Post condition     |  |
|  Nominal Scenario     | Shop manager checks salaries for employees in the current month |
|  Variants     |  |


##### Scenario 7.1

| Scenario 7.1 | Check salaries |
| ------------- |:-------------:| 
|  Precondition     | <ul><li>shop manager wants to manage salaries for the current month</li></ul>|
|  Post condition     | <ul><li>shop manager gets the desired information</li></ul>|
| Step#        | Description  |
|  1     | the system provides information about salaries for the current month  |  

##### Scenario 7.2

| Scenario 7.2 | Update salary for an employee |
| ------------- |:-------------:| 
|  Precondition     | <ul><li>an employee is promoted</li></ul>|
|  Post condition     | <ul><li>salary of employee is properly updated</li></ul>|
| Step#        | Description  |
|  1     | shop manager modifies the standard salary of an employee  |  


# Glossary

```plantuml
	class User
	{
		username 
		password
		name
		surname
		role
		availability
		salary
	}
	class Transaction
	{
		date
		timestamp
		payment method
		value
	}
	class Item
	{
		ID
		threshold
		price
	}
	class FidelityCard
	{
		ID
		credits
		points
	}
	class Balance
	{
		totalBalance
	}

	EzShop -- "*" User
	InventoryManager --|> User
	InventoryManager -- Inventory
	Accountant --|> User
	Accountant -- Balance
	ShopManager --|> User
	EzShop -- Inventory
	Cashier --|> User
	Cashier -- CashRegister : uses
	PaymentGateway -- EzShop
	Item "0..*" -- Inventory
	Transaction -- "*" Item : regards
	Transaction -- "0..1" FidelityCard
	Balance -- "*" Transaction
	Transaction -- "0..1" PaymentGateway
	Balance -- EzShop
	ShopManager -- EzShop
	FidelityCard -- EzShop
	
```

# System Design

```plantuml
	class Computer {}
	note as N1
			Not really meaningful in this case
	end note
```
# Deployment Diagram 
```plantuml
	node Server
	node Client
	Server -- "*" Client
	artifact EzShopApplication
	EzShopApplication -- Server
```

