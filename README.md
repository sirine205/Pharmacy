# Pharmacy Management System

A desktop pharmacy management application developed in Java using JavaFX and SQLite.

The application allows pharmacy staff to manage medicines, customers, shelves, and invoices while demonstrating object-oriented programming concepts such as inheritance, interfaces, and polymorphism.

---

## Features

- Manage medicines (Create, Read, Update, Delete)
- Manage customers
- Generate invoices
- Assign products to shelves
- Loyalty discount system for returning customers
- SQLite database for persistent storage
- JavaFX graphical user interface

---

## Technologies

- Java
- JavaFX
- SQLite
- JDBC

---

## Object-Oriented Design

This project was developed to practice object-oriented programming principles.

### Inheritance

Medicine is the base class.

Two specialized medicine types inherit from it:

- Chemical Medicine
- Homeopathic Medicine

### Interface

Products that can be sold implement the `Vendable` interface.

Examples include:

- Medicines
- Medical equipment

This allows invoices to contain different product types while treating them uniformly.

### Polymorphism

The application manipulates products through the `Vendable` interface, enabling different implementations to be handled in the same way.

---

## Database

The application uses SQLite to store:

- Medicines
- Customers
- Shelves
- Invoices

---

## Learning Objectives



This project helped practice:

- Object-Oriented Programming
- Inheritance
- Interfaces
- Polymorphism
- JavaFX GUI development
- SQLite integration using JDBC
- CRUD operations