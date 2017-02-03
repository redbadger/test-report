#!/bin/sh -e

old_pwd="$PWD"
trap "cd '$old_pwd'" EXIT

for project in test-report lein-test-report test-report-junit-xml lein-test-report-junit-xml; do
  cd $project
  lein install
  cd "$old_pwd"
done

cd example
lein test
