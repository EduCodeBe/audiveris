//----------------------------------------------------------------------------//
//                                                                            //
//                        S y s t e m B o u n d a r y                         //
//                                                                            //
//  Copyright (C) Herve Bitteur 2000-2007. All rights reserved.               //
//  This software is released under the GNU General Public License.           //
//  Contact author at herve.bitteur@laposte.net to report bugs & suggestions. //
//----------------------------------------------------------------------------//
//
package omr.sheet;

import omr.log.Logger;

import omr.util.BrokenLine;

import net.jcip.annotations.NotThreadSafe;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Class <code>SystemBoundary</code> handles the closed boundary of a system
 * as a 2D area, defined by two broken lines, on north and south sides.
 *
 * @author Herv&eacute Bitteur
 * @version $Id$
 */
@NotThreadSafe
public class SystemBoundary
    implements BrokenLine.Listener
{
    //~ Static fields/initializers ---------------------------------------------

    /** Usual logger utility */
    private static final Logger logger = Logger.getLogger(SystemBoundary.class);

    //~ Instance fields --------------------------------------------------------

    /** Related system */
    private final SystemInfo system;

    /** Limit on north side */
    private final BrokenLine north;

    /** Limit on south side */
    private final BrokenLine south;
    private final List<BrokenLine> limits;

    /** Handling of the SystemBoundary is delegated to a Polygon */
    private final Polygon polygon = new Polygon();

    //~ Constructors -----------------------------------------------------------

    //----------------//
    // SystemBoundary //
    //----------------//
    /**
     * Creates a new SystemBoundary object with north and south borders
     * @param north the northern limit
     * @param south the southern limit
     */
    public SystemBoundary (SystemInfo system,
                           BrokenLine north,
                           BrokenLine south)
    {
        if ((north == null) || (south == null)) {
            throw new IllegalArgumentException(
                "SystemBoundary needs non-null limits");
        }

        this.system = system;
        this.north = north;
        this.south = south;
        limits = Arrays.asList(north, south);

        buildPolygon();

        // Register 
        for (BrokenLine line : limits) {
            line.addListener(this);
        }
    }

    //~ Methods ----------------------------------------------------------------

    //-----------//
    // getBounds //
    //-----------//
    /**
     * Report the rectangular bounds that enclose this boundary
     * @return the rectangular bounds
     */
    public Rectangle getBounds ()
    {
        return polygon.getBounds();
    }

    //-----------//
    // getLimits //
    //-----------//
    /**
     * Report the limits as a collection
     * @return the north and south limits
     */
    public List<BrokenLine> getLimits ()
    {
        return limits;
    }

    //---------------//
    // getNorthLimit //
    //---------------//
    /**
     * Report the broken line on northern side
     * @return the northern limit
     */
    public BrokenLine getNorthLimit ()
    {
        return north;
    }

    //---------------//
    // getSouthLimit //
    //---------------//
    /**
     * Report the broken line on southern side
     * @return the southern limit
     */
    public BrokenLine getSouthLimit ()
    {
        return south;
    }

    //----------//
    // contains //
    //----------//
    /**
     * Check whether the provided point lies within the SystemBoundary
     * @param point the provided point
     * @return true if the provided point lies within the SystemBoundary
     */
    public boolean contains (Point point)
    {
        return polygon.contains(point);
    }

    //--------//
    // render //
    //--------//
    /**
     * Paint the SystemBoundary in the provided graphic context.
     *
     * @param g     the Graphics context
     * @param ratio the display zoom ratio
     */
    public void render (Graphics g,
                        double   ratio)
    {
        Graphics2D      g2 = (Graphics2D) g;
        int             radius = north.getStickyDistance();

        // Draw the polygon
        AffineTransform saveAT = g2.getTransform();
        g2.transform(AffineTransform.getScaleInstance(ratio, ratio));
        g2.drawPolygon(polygon);

        // Mark the points
        for (int i = 0; i < polygon.npoints; i++) {
            g2.drawRect(
                polygon.xpoints[i] - radius,
                polygon.ypoints[i] - radius,
                2 * radius,
                2 * radius);
        }

        g2.setTransform(saveAT); // Restore
    }

    //----------//
    // toString //
    //----------//
    @Override
    public String toString ()
    {
        return "{Boundary" + " system#" + system.getId() + " north:" + north +
               " south:" + south + "}";
    }

    //--------//
    // update //
    //--------//
    /**
     * A system boundary line has changed
     * @param brokenLine the modified line
     */
    public void update (BrokenLine brokenLine)
    {
        buildPolygon();
        system.boundaryUpdated();
    }

    //--------------//
    // buildPolygon //
    //--------------//
    private void buildPolygon ()
    {
        polygon.reset();

        // North
        for (Point point : north.getPoints()) {
            polygon.addPoint(point.x, point.y);
        }

        // South (in reverse order)
        List<Point> reverse = new ArrayList<Point>(south.getPoints());
        Collections.reverse(reverse);

        for (Point point : reverse) {
            polygon.addPoint(point.x, point.y);
        }
    }
}
