import java.io.File;
import java.util.List;

import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.Image2D;
import mmlib4j.representation.tree.MorphologicalTree;
import mmlib4j.representation.tree.MorphologicalTreeFiltering;
import mmlib4j.representation.tree.NodeLevelSets;
import mmlib4j.representation.tree.attribute.Attribute;
import mmlib4j.representation.tree.componentTree.ConnectedFilteringByComponentTree;
import mmlib4j.utils.AdjacencyRelation;
import mmlib4j.utils.ImageBuilder;
import mmlib4j.utils.MatlabAdapter;
import mmlib4j.utils.Utils;

public class XuAttributeAG {
	
	GrayScaleImage img, simplifiedImage;
	
	MorphologicalTreeFiltering tree;
	
	int numNode;
	
	NodeLevelSets rootTree;
	
	private NodeLevelSets [] mapNodes;
	
	private double areaR[];
	
	private double volumeR[];	
	
	public XuAttributeAG(Object img, String typeOfTree, boolean debug) {
		
		Utils.debug = debug;
		
		this.img = MatlabAdapter.tGrayScaleImage( img );
			
		this.tree = new ConnectedFilteringByComponentTree( this.img, 
									   					   AdjacencyRelation.getCircular( 1 ), 
									   					   typeOfTree.equals("MaxTree") ? true : false );
		
		this.tree.loadAttribute( Attribute.PERIMETER_EXTERNAL );
		
		this.rootTree = this.tree.getRoot();
		
		this.numNode = this.tree.getNumNode();
		
		init();
		
	}
	
	public int numnode(){
		
		return numNode;
		
	}
	
	private void init() {
		
		/* Pre-processing Energy calculation */	
		
		areaR = new double[ numNode ];
		
		volumeR = new double[ numNode ];
		
		getChildrenAttribute( rootTree );				
		
		mapNodes = new NodeLevelSets[ numNode ];
		
		preProcessing( rootTree );
		
	}	
	
	private void getChildrenAttribute( NodeLevelSets node ) {
		
		List<NodeLevelSets> children = node.getChildren();		
			
		areaR[ node.getId() ] = node.getAttributeValue( Attribute.AREA );
		
		volumeR[ node.getId() ] = node.getAttributeValue( Attribute.VOLUME );		
		
		for( NodeLevelSets son : children ) {
			
			getChildrenAttribute( son );
			
		}
		
	}
	
	private void preProcessing( NodeLevelSets node ) {
		
		List<NodeLevelSets> children = node.getChildren();	
		
		NodeLevelSets parent = node.getParent();
				
		if( parent != null ) {
			
			if( node.getParent() != rootTree ) {
				
				areaR[ node.getParent().getId() ] -= areaR[ node.getId() ];
				
				volumeR[ node.getParent().getId() ] -= volumeR[ node.getId() ];			
			
			}	
			
		}			
		
		mapNodes[ node.getId() ] = node;			
		
		for( NodeLevelSets son : children ) {
			
			preProcessing( son );
			
		}
		
	}
	
	private double pow2( double v ) {
		
		return v*v;
		
	}
	
	private double calculateFuncional( NodeLevelSets node ) { 
		
		NodeLevelSets parent = node.getParent();
		
		double p1 = pow2( volumeR[ node.getId() ] ) / areaR[ node.getId() ];
		
		double p2 = pow2( volumeR[ parent.getId() ] ) / areaR[ parent.getId() ];
		
		double p3 = pow2( volumeR[ node.getId() ] + volumeR[ parent.getId() ] ) / ( areaR[ node.getId() ] + areaR[ parent.getId() ] );
		
		return ( p1 + p2 - p3 ) / node.getAttributeValue( Attribute.PERIMETER_EXTERNAL );
		
	}
	
	public double fitness( int id ) {
		
		return (id == 0 || mapNodes[id] == null) ? Double.POSITIVE_INFINITY : calculateFuncional( mapNodes[id] ); 
		
	}
	
	public void remove( int id ) {
		
		NodeLevelSets node = mapNodes[id];
		
		areaR[ node.getParent().getId() ] += areaR[ node.getId() ];
		
		volumeR[ node.getParent().getId() ] += volumeR[ node.getId() ];
		
		((MorphologicalTree) this.tree).mergeFather( mapNodes[id] );
		
		mapNodes[id] = null;
		
	}
	
	public Object result() {
		
		if( simplifiedImage == null ) {
			
			simplifiedImage = ((MorphologicalTree) this.tree).reconstruction();
					
		}
		
		return MatlabAdapter.toMatlab( (Image2D) simplifiedImage );
		
	}
	
	public static void main( String args [] ) {
		
		GrayScaleImage img = ImageBuilder.openGrayImage(new File("/home/gobber/Desktop/mumford-image-test.png"));
		
		XuAttributeAG solution = new XuAttributeAG(img, "TreeOfShapes", true);
		
		System.out.println("Energy = "+ solution.fitness(10));
		
		for(int i=1000; i < solution.numNode; i++){
			
			solution.remove( i );
			
		}
		
		ImageBuilder.saveImage( MatlabAdapter.tGrayScaleImage( solution.result() ), new File("/home/gobber/reconstructed.png"));
		
	}

}
