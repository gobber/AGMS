import java.io.File;

import mmlib4j.images.GrayScaleImage;
import mmlib4j.representation.tree.NodeLevelSets;
import mmlib4j.representation.tree.attribute.Attribute;
import mmlib4j.representation.tree.componentTree.ConnectedFilteringByComponentTree;
import mmlib4j.utils.AdjacencyRelation;
import mmlib4j.utils.ImageBuilder;
import mmlib4j.utils.MatlabAdapter;
import mmlib4j.utils.Utils;

public class FunctionalAttribute extends MumfordShahFunctionalMorphologicalTree {	
	
	public FunctionalAttribute(Object img, String typeOfTree, boolean debug) {
		
		Utils.debug = debug;
		
		if( img instanceof GrayScaleImage ){
			this.img = (GrayScaleImage) img;
		} else{
			this.img = MatlabAdapter.tGrayScaleImage( img );
		}
			
		this.tree = new ConnectedFilteringByComponentTree( this.img, 
									   					   AdjacencyRelation.getCircular( 1 ), 
									   					   typeOfTree.equals("MaxTree") ? true : false );
		
		this.tree.loadAttribute( Attribute.PERIMETER_EXTERNAL );
		
		this.rootTree = this.tree.getRoot();
		
		this.numNode = this.tree.getNumNode();
		
		init();
		
	}
		
	public double calculateEnergy( NodeLevelSets node ) { 
		
		NodeLevelSets parent = node.getParent();
		
		double p1 = pow2( volumeR[ node.getId() ] ) / areaR[ node.getId() ];
		
		double p2 = pow2( volumeR[ parent.getId() ] ) / areaR[ parent.getId() ];
		
		double p3 = pow2( volumeR[ node.getId() ] + volumeR[ parent.getId() ] ) / ( areaR[ node.getId() ] + areaR[ parent.getId() ] );
		
		return ( p1 + p2 - p3 ) / node.getAttributeValue( Attribute.PERIMETER_EXTERNAL );
		
	}
	
	public static void main( String args [] ) {
		
		GrayScaleImage img = ImageBuilder.openGrayImage(new File("/home/gobber/Desktop/mumford-image-test.png"));
		
		FunctionalAttribute solution = new FunctionalAttribute(img, "MaxTree", true);
		
		System.out.println("Energy = "+ solution.fitness(10));
		
		for(int i=1000; i < solution.numNode; i++){
			
			solution.remove( i );
			
		}
		
		ImageBuilder.saveImage( MatlabAdapter.tGrayScaleImage( solution.result() ), new File("/home/gobber/reconstructed.png"));
		
	}

}
