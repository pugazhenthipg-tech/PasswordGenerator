# Password Generator

A Java console password generator powered by `java.security.SecureRandom`.

## Features

- Configurable password length (`4` to `128` characters)
- Selectable character classes: uppercase, lowercase, digits, and symbols
- Guarantees at least one character from each enabled character class
- Option to exclude ambiguous characters (`O0oIl1|`)
- Non-interactive `--demo` mode

## Compile and Run

```bash
javac PasswordGenerator.java
java PasswordGenerator
```

Run demo mode:

```bash
java PasswordGenerator --demo
```
