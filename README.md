# 🪸 Coral Programming Language

**Coral** is a lightweight, expressive, and embeddable programming language designed for learning, rapid prototyping and self-teaching purposes. It’s written in **Kotlin**, and features a clean, modular architecture with its own Lexer, Parser, AST, and Interpreter components.

> 🌊 *Built for simplicity, clarity, and creativity.*

---

## ✨ Features

- 🧠 **Simple Syntax** — Easy to read and write, perfect for learning language design.
- ⚙️ **Custom Parser & AST** — Includes a hand-rolled recursive descent parser.
- 🔤 **Lexical Analysis** — Tokenizer supports basic operators, control flow, and more.
- 🧩 **First-Class Functions** — Function literals and function calls are supported.
- 🧪 **JUnit Tests** — Comes with comprehensive test cases to validate parsing and lexing.
- 📦 **Modular Design** — Easy to extend with new features like loops, classes, etc.

---

## 🔍 Example Code

```coral
let max = fn(x, y) {
  if ( x > y ) {
    return x;
  } else {
    return y;
  }
};

let result = max(2, 3);
```

## 📦 Getting Started

### Clone the repo

```bash
git clone https://github.com/LinusCode146/Coral.git
```

## Run tests
```bash
./gradlew test
```

### 🧭 Explore the code

The Coral codebase is organized into the following key packages:

- [`main.coral.lexer`](src/main/kotlin/main/coral/lexer) — Responsible for turning source code into a stream of tokens.
- [`main.coral.parser`](src/main/kotlin/main/coral/parser) — Parses the tokens into an Abstract Syntax Tree (AST).
- [`main.coral.ast`](src/main/kotlin/main/coral/ast) — Contains definitions for AST nodes representing Coral programs.
- [`test`](src/test/kotlin) — Unit tests for the lexer and parser using JUnit 5.

