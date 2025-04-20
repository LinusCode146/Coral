🪸 Coral Programming Language
Coral is a lightweight, expressive, and embeddable programming language designed for learning, hacking, and rapid prototyping. It’s written in Kotlin, and features a clean, modular architecture with its own Lexer, Parser, AST, and Interpreter components.

🌊 Inspired by the simplicity of Lisp, the elegance of Python, and the structure of Go.

✨ Features
🧠 Simple Syntax — Easy to read and write, perfect for learning language design.

⚙️ Custom Parser & AST — Includes a hand-rolled recursive descent parser.

🔤 Lexical Analysis — Tokenizer supports basic operators, control flow, and more.

🧩 First-Class Functions — Function literals and function calls are supported.

🧪 JUnit Tests — Comes with comprehensive test cases to validate parsing and lexing.

📦 Modular Design — Easy to extend with new features like loops, classes, etc.

🔍 Example Code
coral
Kopieren
Bearbeiten
let add = fn(x, y) {
  return x + y;
};

let result = add(2, 3);
📦 Getting Started
Clone the repo
bash
Kopieren
Bearbeiten
git clone https://github.com/yourusername/coral.git
cd coral
Run tests
bash
Kopieren
Bearbeiten
./gradlew test
Explore the code
main.coral.lexer — Lexer/tokenizer

main.coral.parser — Recursive descent parser

main.coral.ast — Abstract Syntax Tree definitions

test — Lexer and parser test cases using JUnit 5

🛠 Build From Source
Coral is built using Gradle with Kotlin:

bash
Kopieren
Bearbeiten
./gradlew build
🚧 Roadmap
 Basic expressions and statements

 Prefix and infix operators

 Let and return statements

 Function literals and calls

 Support for strings and arrays

 Loops (while, for)

 A REPL / Interpreter

 Compile to bytecode or other target languages

📚 Learn More
Interested in language design or interpreters? Coral is a great place to start hacking and learning. It’s inspired by:

Writing An Interpreter In Go

Crafting Interpreters

The Little Schemer

🤝 Contributing
Pull requests are welcome! Whether you want to fix a bug, add a feature, or improve documentation, feel free to open an issue or PR.

🧑‍💻 Author
[Your Name]
💻 GitHub: @yourusername
🐦 Twitter: @yourhandle

📄 License
This project is licensed under the MIT License — see the LICENSE file for details.

“Code is like coral — it grows slowly, layer by layer, into something beautiful.” 🪸
