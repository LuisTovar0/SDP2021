server:
	rm -rf out/
	ant
	cp src/*.jks src/*.pem out/
	cd out; java ServerMain

client:
	rm -rf out/
	ant
	cp src/*.jks src/*.pem out/
	cd out/; java ClienteEmissor

alojador:
	rm -rf out/
	ant
	cp src/*.jks src/*.pem out/
	cd out/; java Alojador

distribuicao:
	rm -rf out/
	ant
	cp src/*.jks src/*.pem out/
	cd out/; java CentroDistribuicaoMain

