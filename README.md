# 🪸 Coral Programming Language

**Coral** is a lightweight, expressive, and embeddable programming language designed for learning, hacking, and rapid prototyping. It’s written in **Kotlin**, and features a clean, modular architecture with its own Lexer, Parser, AST, and Interpreter components.

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
let add = fn(x, y) {
  return x + y;
};

let result = add(2, 3);
```

## 📦 Getting Started

### Clone the repo

```bash
git clone https://github.com/yourusername/coral.git
cd coral
```

## Run tests
```bash
./gradlew test
```
