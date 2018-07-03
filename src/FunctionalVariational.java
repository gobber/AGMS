import java.io.File;

import mmlib4j.images.GrayScaleImage;
import mmlib4j.representation.tree.NodeLevelSets;
import mmlib4j.representation.tree.attribute.Attribute;
import mmlib4j.representation.tree.componentTree.ConnectedFilteringByComponentTree;
import mmlib4j.utils.AdjacencyRelation;
import mmlib4j.utils.ImageBuilder;
import mmlib4j.utils.MatlabAdapter;
import mmlib4j.utils.Utils;

public class FunctionalVariational extends MumfordShahFunctionalMorphologicalTree {
	
	double scale = 1.0;
	
	public FunctionalVariational(Object img, String typeOfTree, boolean debug, double scale) {
		
		Utils.debug = debug;
		
		this.scale = scale;
			
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
		
		double t1 = pow2( volumeR[ node.getId() ] ) / areaR[ node.getId() ];
		
		double t2 = pow2( volumeR[ node.getParent().getId() ] ) / areaR[ node.getParent().getId() ];
		
		double t3 = pow2( volumeR[ node.getId() ] + volumeR[ node.getParent().getId() ] ) / ( areaR[ node.getId() ] + areaR[ node.getParent().getId() ] );
		
		return ( t1 + t2 - t3 ) - ( scale * node.getAttributeValue( Attribute.PERIMETER_EXTERNAL ) );
		
	}
	
	public static void main( String args [] ) {
		
		GrayScaleImage img = ImageBuilder.openGrayImage(new File("/home/gobber/Desktop/mumford-image-test.png"));
		
		FunctionalVariational solution = new FunctionalVariational(img, "MaxTree", true, 1);
		
		System.out.println("Energy = "+ solution.fitness(10));
		
		for(int i=1000; i < solution.numNode; i++){
			
			solution.remove( i );
			
		}
		
		ImageBuilder.saveImage( MatlabAdapter.tGrayScaleImage( solution.result() ), new File("/home/gobber/reconstructed.png"));
		
	}
	
}
