# Springlogowanie - System Rekomendacji w E-commerce

## Opis projektu
Springlogowanie to aplikacja e-commerce z systemem rekomendacji produktów, opartym na Content-Based Filtering z użyciem Apache Lucene. Użytkownicy mogą przeglądać produkty, dodawać je do koszyka, składać zamówienia i otrzymywać spersonalizowane rekomendacje.

## Wymagania
- Java 17
- Maven 3.6+
- PostgreSQL (Neon w chmurze AWS)
- Opcjonalnie: IDE (np. IntelliJ IDEA)

## Dane do logowania do PostgreSQL (Neon)
- **Nazwa użytkownika**: `neondb_owner`
- **Hasło**: `npg_8Odh2UeIXMHP`
- **URL bazy danych**: `jdbc:postgresql://ep-dawn-waterfall-a2fha4mx-pooler.eu-central-1.aws.neon.tech:5432/neondb?sslmode=require`
- **Nazwa bazy danych**: `neondb`

## Instrukcja importu bazy danych
1. Upewnij się, że masz dostęp do bazy danych Neon (konfiguracja jest w pliku `application.properties`).
2. Zaimportuj plik `projekt_db.sql` do bazy danych za pomocą narzędzia SQL (np. DBeaver lub psql):
   ```bash
   psql -h ep-dawn-waterfall-a2fha4mx-pooler.eu-central-1.aws.neon.tech -p 5432 -U neondb_owner -d neondb < baza licencjat.sql
   
-lub można użyć generatedata w SpringlogowanieApplication aby wygenerować dane w bazie. 

## Plik główny

- SpringLogowanieApplication
- Tym plikiem uruchamia sie aplikacje.