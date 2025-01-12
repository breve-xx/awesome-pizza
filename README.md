# Awesome Pizza, awesome API

This project is a RESTful API built with **Java 21** and **Spring Boot**, designed to handle pizza orders. The application employs **MongoDB** as its persistence layer and utilizes **Gradle** for dependency management. The API is divided into two main operational flows:

1. **User Operations**:
    - Submit a new order.
    - Check the status of an existing order.

2. **Cook Operations**:
    - View the list of orders.
    - Update the status of orders as they progress through preparation.

## Key Features

- **Modern Java**: Built with Java 21.
- **Spring Boot Framework**: Simplifies the development of RESTful APIs with a robust and scalable foundation.
- **MongoDB Integration**: Ensures efficient and flexible data persistence for storing orders and their details.
- **API Documentation**:
    - Uses `org.springdoc:springdoc-openapi-starter-webmvc-ui:2.2.0` to provide an OpenAPI-compliant interface for exploring and testing API endpoints.
- **Docker Support**:
    - A `docker-compose.yml` file is provided under the `local-docker-services` directory to facilitate launching a local MongoDB instance for development and testing.

## Building and Running the Application Locally

Follow these steps to build and run the application locally:

### Prerequisites
- **Java 21** installed.
- **Gradle** installed (or use the Gradle Wrapper included in the project).
- **Docker** installed for running MongoDB.

### Steps

1. **Start MongoDB**:
    - Navigate to the `local-docker-services` directory:
      ```bash
      cd local-docker-services
      ```
    - Use Docker Compose to launch the MongoDB instance:
      ```bash
      docker-compose up -d
      ```

2. **Build the Application**:
    - Use the following command to build the project:
      ```bash
      ./gradlew clean build
      ```

3. **Run the Application**:
    - Start the application using Gradle:
      ```bash
      ./gradlew bootRun
      ```
    - The application will be available at `http://localhost:8080`.

4. **Access API Documentation**:
    - Visit `http://localhost:8080/swagger-ui.html` to explore and test the API endpoints using the Swagger UI.

## Notes
- The application is designed for seamless development and testing, with the Docker-based MongoDB instance simplifying local setup.
- The OpenAPI documentation ensures that both developers and external consumers can easily understand and interact with the API.


## Endpoints

### `/`

#### GET: Just say hello!
Displays basic API information.

- **Operation ID**: `hello_1`
- **Responses**:
    - `200 OK`: Returns a string containing basic API info.

---

### `/api/v1/order`

#### GET: It's your turn now!
Retrieve the next available order in the queue if no order is currently in progress.

- **Operation ID**: `list_1`
- **Responses**:
    - `200 OK`: Returns an array of `Order` objects.

#### POST: Submit an order
Add your favorite pizza to the queue and get it delivered as soon as possible.

- **Operation ID**: `submit`
- **Request Body**:
    - Content-Type: `application/json`
    - Schema: `SubmitOrderRequest`
- **Responses**:
    - `200 OK`: Returns a `SubmitOrderResponse` containing the order code for tracking.
    - `400 Bad Request`: The request cannot be processed.

---

### `/api/v1/order/{orderCode}`

#### GET: Are you hungry?
Retrieve the status of a specific order using its `orderCode`.

- **Operation ID**: `get`
- **Parameters**:
    - `orderCode` (path, string, required): The code of the order to query.
- **Responses**:
    - `200 OK`: Returns the `Order` object with details and status.
    - `404 Not Found`: Order not found.

#### PATCH: Let's work on it
Update the status of an order using its `orderCode`.

- **Operation ID**: `update`
- **Parameters**:
    - `orderCode` (path, string, required): The code of the order to update.
    - `status` (query, string, required): The new status. Allowed values: `READY`, `IN_PROGRESS`, `DELIVERED`.
- **Responses**:
    - `200 OK`: Status successfully updated.
    - `422 Unprocessable Entity`: Update rules violation.
    - `404 Not Found`: Order not found.

---

### `/api/v1/pizza`

#### GET: All the available Pizzas
Retrieve the list of pizzas available for ordering.

- **Operation ID**: `list`
- **Responses**:
    - `200 OK`: Returns an array of strings. Allowed values: `MARGHERITA`, `CAPRICCIOSA`, `DIAVOLA`.

---

## Components

### Schemas

#### `Order`
Represents an order.

- **Properties**:
    - `id` (string, uuid): Unique identifier for the order.
    - `submittedAt` (string, date-time): The time the order was submitted.
    - `status` (string, enum): Status of the order. Allowed values: `READY`, `IN_PROGRESS`, `DELIVERED`.
    - `pizzas` (object): A mapping of pizza names to quantities.

#### `SubmitOrderRequest`
Represents the request body for submitting an order.

- **Properties**:
    - `order` (array of `OrderEntry`): List of pizzas in the order.

#### `SubmitOrderResponse`
Represents the response after submitting an order.

- **Properties**:
    - `orderCode` (string, uuid): The unique tracking code for the order.

#### `OrderEntry`
Represents a single pizza entry in an order.

- **Properties**:
    - `name` (string): Name of the pizza.
    - `qty` (integer, int32): Quantity of the pizza.

---

## Rules for Updating Orders
1. Peek `READY` orders one at a time.
2. Deliver only `IN_PROGRESS` orders.
3. Never revert an order back to `READY`.
