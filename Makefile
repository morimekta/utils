site:
	rm -rf morimekta.github.io/utils/*
	mvn clean verify site:site site:stage
	mkdir -p morimekta.github.io/utils/coverage-report
	cp -R testing/target/coverage-report/html/.resources testing/target/coverage-report/html/*  morimekta.github.io/utils/coverage-report/
