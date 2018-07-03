import java.util.List;

import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.Image2D;
import mmlib4j.representation.tree.MorphologicalTree;
import mmlib4j.representation.tree.MorphologicalTreeFiltering;
import mmlib4j.representation.tree.NodeLevelSets;
import mmlib4j.representation.tree.attribute.Attribute;
import mmlib4j.utils.MatlabAdapter;


public abstract class MumfordShahFunctionalMorphologicalTree {
	
	GrayScaleImage img, simplifiedImage;
	
	MorphologicalTreeFiltering tree;
	
	int numNode;
	
	NodeLevelSets rootTree;
	
	NodeLevelSets [] mapNodes;
	
	double areaR[];
	
	double volumeR[];
	
	public int numnode(){
		
		return numNode;
		
	}
	
	public void init() {
		
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
	
	public double pow2( double v ) {
		
		return v*v;
		
	}
	
	public double fitness( int id ) {
		
		return (id == 0 || mapNodes[id] == null) ? Double.POSITIVE_INFINITY : calculateEnergy( mapNodes[id] ); 
		
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
	
	abstract public double calculateEnergy( NodeLevelSets node );

}
