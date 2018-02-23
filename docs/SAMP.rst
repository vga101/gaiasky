SAMP integration
****************

As of commit `4d0d133 <https://github.com/langurmonkey/gaiasky/commit/4d0d13304d1e2b6991ad2cc84429a37083ae0954>`__, or
version ``2.0.0``, Gaia Sky supports interoperability via `SAMP <http://www.ivoa.net/documents/SAMP/>`__.
However, due to the nature of Gaia Sky, not all functions are yet implemented and not all types of data tables
are supported.

Since Gaia Sky only displays 3D positional information there are a few restrictions as to how the integration with SAMP is implemneted.

The current implementation only allows using Gaia Sky as a SAMP client. This means that
when Gaia Sky is started, it automatically looks for a preexisting SAMP hub. If it is found, then
a connection is attempted. If it is not found, then Gaia Sky will attempt further
connections at regular intervals of 10 seconds. Gaia Sky will
never run its own SAMP hub, so the user always needs a SAMP-hub application (Topcat,
Aladin, etc.) to use the interoperability that SAMP offers.

Also, the only supported format is VOTable. There are, however, some restrictions
as to how the VOTables received through SAMP are interpreted and used (if at all)
by Gaia Sky:

-  For the **positional data**, Gaia Sky will look for spherical and cartesian coordinates. In the
case of spherical coordinates, the following are supported: 
    -  Equatorial (`pos.eq.ra`, `pos.eq.dec`)
    -  Galactic (`pos.galactic.lon`, `pos.galactic.lat`)
    -  Ecliptic (`pos.ecliptic.lon`, `pos.ecliptic.lat`)
To work out the distance, it looks for `pos.parallax` and `pos.distance`. If either of those are found, they are used. Otherwise, a default parallax of 0.04 mas is used. 
With respect to cartesian coordinates, it recognizes `pos.cartesian.x|y|z`, and they are interpreted in the equatorial system by default.
If no UCDs are available, only equatorial coordinates (ra, dec) are supported, and they are looked up using
the column names.
-  **Proper motions** are not yet supported via SAMP.
-  **Magnituded** are supported using the `phot.mag` or `phot.mag;stat.mean` UCDs. Otherwise, they are
discovered using the column names `mag`, `bmag`, `gmag`, `phot_g_mean_mag`. If no magnitudes are found,
the default value of 15 is used.
-  **Colors** are discovered using the `phot.color` UCD. If not present, the column names `b_v`, `v_i`,
`bp_rp`, `bp_g` and `g_rp` are used, if present. If no color is discovered at all, the default value of 0.656 is used.
- Other physical quantities (mass, flux, T_eff, radius, etc.) are not yet supported via SAMP.

Implemented features
====================

The following features are implemented:

-  Load VOTable (``table.load.votable``) - The VOTable will be loaded into Gaia Sky if it adheres to the format above.
-  Highlight row (``table.highlight.row``) - The row (object) is set as the new focus if the table it comes frome is already loaded. Otherwise, Gaia Sky will **not** load the table lazily.
-  Broadcast selection (``table.highlight.row``) - When a star of a table loaded via SAMP is selected, Gaia Sky broadcasts it as a row highlight, so that other clients may act on it.

Unimplemented features
======================

The following functions are not yet implemented:

-  Multi selection (``table.select.rowList``) - To be implemented when multi-selections are included in Gaia Sky.
-  Point at sky (``coord.pointAt.sky``) - To be implemented soon, as a simple camera movement in free mode.
