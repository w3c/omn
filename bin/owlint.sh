#!/usr/bin/env bash

if ! type owlint >/dev/null 2>&1; then
  echo "Installing owlint..."
  npm install -g owlint
fi

echo "Running owlint..."
owlint $@