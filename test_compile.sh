#!/bin/bash
echo "Testing compilation..."
mvn clean compile -q
if [ $? -eq 0 ]; then
    echo "✅ Compilation successful!"
else
    echo "❌ Compilation failed!"
fi