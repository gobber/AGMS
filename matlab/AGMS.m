% =======================================================
%
%  Entrada: imgFile    = Caminho da imagem
%           typeOfTree = MaxTree ou MinTree
%           debug      = debug da mmlib4j (true = ligado, false = desligado)
%
%  Saída:   solution   =  instância da classe.
%
% =======================================================
function [ solution ] = AGMS( imgFile, typeOfTree, debug )
    
    javaclasspath({'../libraries/mmlib4j-api-1.1.jar', '../bin/'});
    
    img = im2int16(rgb2gray(imread(imgFile)));
    
    solution = FunctionalAttribute(img, typeOfTree, debug);

end
