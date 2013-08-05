import java.util.Date;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * Date: 19/07/13
 * Time: 15:32
 */
public class AnalyseData
{
    private static final Vector<Fingerprint> fingerprints = new Vector<Fingerprint>();

    public static void main(String[] args)
    {
        StringTokenizer stringTokenizer = new StringTokenizer(JSON_DATA);
        while(stringTokenizer.hasMoreTokens())
        {
            stringTokenizer.nextToken(); // ignore
            final String t = stringTokenizer.nextToken();
            final String w = stringTokenizer.nextToken();
            final String lat = stringTokenizer.nextToken();
            final String lng = stringTokenizer.nextToken();
            stringTokenizer.nextToken(); // ignore

            Fingerprint fingerprint = new Fingerprint(t, w, lat, lng);
//            System.out.println(fingerprint);

            fingerprints.add(fingerprint);
        }

        System.out.println("# of fingerprints: " + fingerprints.size());

        Vector<Fingerprint> homeFingerprints = new Vector<Fingerprint>();
        Vector<Fingerprint> workFingerprints = new Vector<Fingerprint>();
        Vector<Fingerprint> elseFingerprints = new Vector<Fingerprint>();
        for(Fingerprint fingerprint : fingerprints)
        {
            if(fingerprint.where == 'H') homeFingerprints.add(fingerprint);
            else if(fingerprint.where == 'W') workFingerprints.add(fingerprint);
            else if(fingerprint.where == 'E') elseFingerprints.add(fingerprint);
            // else ignore
        }

        final int MIN_TRAINING_SIZE = 10;

        final Vector<Cluster> homeClusters = formClusters(homeFingerprints);
        System.out.println("Home clusters");
        for(final Cluster homeCluster : homeClusters)
        {
            if(homeCluster.size() < MIN_TRAINING_SIZE)
            {
                // ignore
            }
            else
            {
                System.out.println("* " + homeCluster);
            }
        }

        final Cluster homeCluster = selectDominantCluster(homeClusters);
        System.out.println("Dominant home cluster");
        System.out.println("** " + homeCluster);

        final Vector<Cluster> workClusters = formClusters(workFingerprints);
        System.out.println("Work clusters");
        for(final Cluster workCluster : workClusters)
        {
            if(workCluster.size() < MIN_TRAINING_SIZE)
            {
                // ignore
            }
            else
            {
                final float distanceToHome;
                if(homeCluster != null)
                {
                    distanceToHome = distanceBetween(workCluster.getCenter(), homeCluster.getCenter());
                }
                else
                {
                    distanceToHome = Float.MAX_VALUE;
                }

                if(distanceToHome > CLUSTER_RADIUS) // ignore locations too close to home
                {
                    System.out.println("* " + workCluster + ", distance to home: " + distanceToHome);
                }
            }
        }

        final Cluster workCluster = selectDominantCluster(workClusters); // todo perhaps do not count clusters which are too close to home
        System.out.println("Dominant work cluster");
        System.out.println("** " + workCluster);

        final Vector<Cluster> elsewhereClusters = formClusters(elseFingerprints);
        System.out.println("Elsewhere clusters");
        for(final Cluster elsewhereCluster : elsewhereClusters)
        {
            if(elsewhereCluster.size() < MIN_TRAINING_SIZE)
            {
                // ignore
            }
            else
            {
                final float distanceToHome = homeCluster == null ? Float.NaN : distanceBetween(elsewhereCluster.getCenter(), homeCluster);
                System.out.println("* " + elsewhereCluster + ", distance to home: " + distanceToHome);
            }
        }
    }

    static Cluster selectDominantCluster(final Vector<Cluster> clusters)
    {
        if(clusters == null || clusters.size() == 0) throw new IllegalArgumentException("Clusters argument must be not null and non-empty!");

        // for now just select a cluster with max size

        Cluster dominantCluster = null;
        int maxSize = 0;
        for(final Cluster cluster : clusters)
        {
            if(cluster.size() > maxSize)
            {
                dominantCluster = cluster;
                maxSize = cluster.size();
            }
        }

        return dominantCluster;
    }

    public static final float CLUSTER_RADIUS = 100f; // 100m

    static private Vector<Cluster> formClusters(final Vector<Fingerprint> fingerprints)
    {
        final Vector<Cluster> clusters = new Vector<Cluster>();

        for(final Fingerprint fingerprint : fingerprints)
        {
            boolean added = false;
            final Coordinates coordinates = fingerprint.coordinates;
            for(final Cluster cluster : clusters)
            {
                if(distanceBetween(coordinates, cluster) < CLUSTER_RADIUS)
                {
                    cluster.addMember(coordinates);
                    added = true;
                }
            }
            if(!added) // create new cluster
            {
                final Cluster cluster = new Cluster();
                cluster.addMember(coordinates);
                clusters.add(cluster);
            }
        }

        return clusters;
    }

    static private float distanceBetween(final Coordinates coordinates, final Cluster cluster)
    {
        return distanceBetween(coordinates, cluster.getCenter());
    }

    static private float distanceBetween(final Coordinates coordinates1, final Coordinates coordinates2)
    {
        return computeDistance(coordinates1.lat, coordinates1.lng, coordinates2.lat, coordinates2.lng);
    }

    static private class Cluster
    {
        private final Vector<Coordinates> members = new Vector<Coordinates>();

        void addMember(final Coordinates coordinates)
        {
            members.add(coordinates);
        }

        Coordinates getCenter()
        {
            final int size = members.size();

            if(size > 0)
            {
                double averageLat = 0d;
                double averageLng = 0d;
                for(final Coordinates coordinates : members)
                {
                    averageLat += coordinates.lat;
                    averageLng += coordinates.lng;
                }

                return new Coordinates(averageLat/size, averageLng/size);
            }
            else
            {
                return null;
            }
        }

        Coordinates getStandardDeviation()
        {
            final Coordinates center = getCenter();

            double devLat = 0d;
            double devLng = 0d;
            for(final Coordinates member : members)
            {
                devLat += Math.pow(member.lat - center.lat, 2);
                devLng += Math.pow(member.lng - center.lng, 2);
            }

            return new Coordinates(Math.sqrt(devLat/members.size()), Math.sqrt(devLng/members.size()));
        }

        int size()
        {
            return members.size();
        }

        @Override public String toString()
        {
            return super.toString() + ", Size: " + members.size() + ", Center: " + getCenter();
        }
    }

    static private class Coordinates
    {
        double lat;
        double lng;

        Coordinates(String lat, String lng)
        {
            this(Double.parseDouble(lat), Double.parseDouble(lng));
        }

        Coordinates(final double lat, final double lng)
        {
            this.lat = lat;
            this.lng = lng;
        }

        @Override public String toString()
        {
            return "(" + lat + " " + lng + ")";
        }
    }

    static private class Fingerprint
    {
        long timestamp;
        char where;
        Coordinates coordinates;

        Fingerprint(String t, String w, String lat, String lng)
        {
            timestamp = Long.parseLong(t);
            where = w.charAt(0);
            coordinates = new Coordinates(lat, lng);
        }

        @Override public String toString()
        {
            return new Date(timestamp) + " " + where + " " + coordinates;
        }
    }

    static private float computeDistance(double lat1, double lon1, double lat2, double lon2)
    {
        // Based on http://www.ngs.noaa.gov/PUBS_LIB/inverse.pdf using the "Inverse Formula" (section 4)

        int MAXITERS = 20;
        // Convert lat/long to radians
        lat1 *= Math.PI / 180.0;
        lat2 *= Math.PI / 180.0;
        lon1 *= Math.PI / 180.0;
        lon2 *= Math.PI / 180.0;

        double a = 6378137.0; // WGS84 major axis
        double b = 6356752.3142; // WGS84 semi-major axis
        double f = (a - b) / a;
        double aSqMinusBSqOverBSq = (a * a - b * b) / (b * b);

        double L = lon2 - lon1;
        double A = 0.0;
        double U1 = Math.atan((1.0 - f) * Math.tan(lat1));
        double U2 = Math.atan((1.0 - f) * Math.tan(lat2));

        double cosU1 = Math.cos(U1);
        double cosU2 = Math.cos(U2);
        double sinU1 = Math.sin(U1);
        double sinU2 = Math.sin(U2);
        double cosU1cosU2 = cosU1 * cosU2;
        double sinU1sinU2 = sinU1 * sinU2;

        double sigma = 0.0;
        double deltaSigma = 0.0;
        double cosSqAlpha = 0.0;
        double cos2SM = 0.0;
        double cosSigma = 0.0;
        double sinSigma = 0.0;
        double cosLambda = 0.0;
        double sinLambda = 0.0;

        double lambda = L; // initial guess
        for (int iter = 0; iter < MAXITERS; iter++)
        {
            double lambdaOrig = lambda;
            cosLambda = Math.cos(lambda);
            sinLambda = Math.sin(lambda);
            double t1 = cosU2 * sinLambda;
            double t2 = cosU1 * sinU2 - sinU1 * cosU2 * cosLambda;
            double sinSqSigma = t1 * t1 + t2 * t2; // (14)
            sinSigma = Math.sqrt(sinSqSigma);
            cosSigma = sinU1sinU2 + cosU1cosU2 * cosLambda; // (15)
            sigma = Math.atan2(sinSigma, cosSigma); // (16)
            double sinAlpha = (sinSigma == 0) ? 0.0 :
                    cosU1cosU2 * sinLambda / sinSigma; // (17)
            cosSqAlpha = 1.0 - sinAlpha * sinAlpha;
            cos2SM = (cosSqAlpha == 0) ? 0.0 :
                    cosSigma - 2.0 * sinU1sinU2 / cosSqAlpha; // (18)

            double uSquared = cosSqAlpha * aSqMinusBSqOverBSq; // defn
            A = 1 + (uSquared / 16384.0) * // (3)
                    (4096.0 + uSquared *
                            (-768 + uSquared * (320.0 - 175.0 * uSquared)));
            double B = (uSquared / 1024.0) * // (4)
                    (256.0 + uSquared *
                            (-128.0 + uSquared * (74.0 - 47.0 * uSquared)));
            double C = (f / 16.0) *
                    cosSqAlpha *
                    (4.0 + f * (4.0 - 3.0 * cosSqAlpha)); // (10)
            double cos2SMSq = cos2SM * cos2SM;
            deltaSigma = B * sinSigma * // (6)
                    (cos2SM + (B / 4.0) *
                            (cosSigma * (-1.0 + 2.0 * cos2SMSq) -
                                    (B / 6.0) * cos2SM *
                                            (-3.0 + 4.0 * sinSigma * sinSigma) *
                                            (-3.0 + 4.0 * cos2SMSq)));

            lambda = L +
                    (1.0 - C) * f * sinAlpha *
                            (sigma + C * sinSigma *
                                    (cos2SM + C * cosSigma *
                                            (-1.0 + 2.0 * cos2SM * cos2SM))); // (11)

            double delta = (lambda - lambdaOrig) / lambda;
            if (Math.abs(delta) < 1.0e-12) {
                break;
            }
        }

        float distance = (float) (b * A * (sigma - deltaSigma));

        return distance;
    }

    public static final String JSON_DATA =
            "    {" +
            "      1368909795631" +
            "      H" +
            "      34.713802" +
            "      33.16179" +
            "    }" +
            "    {" +
            "      1368907686001" +
            "      H" +
            "      34.713223" +
            "      33.16159" +
            "    }" +
            "    {" +
            "      1368902753499" +
            "      H" +
            "      34.71348" +
            "      33.161587" +
            "    }" +
            "    {" +
            "      1368900897820" +
            "      H" +
            "      34.713524" +
            "      33.161488" +
            "    }" +
            "    {" +
            "      1368897936530" +
            "      H" +
            "      34.7134" +
            "      33.161613" +
            "    }" +
            "    {" +
            "      1368897121551" +
            "      H" +
            "      34.716095" +
            "      33.161804" +
            "    }" +
            "    {" +
            "      1368896521774" +
            "      H" +
            "      34.695347" +
            "      33.0451" +
            "    }" +
            "    {" +
            "      1368895751402" +
            "      E" +
            "      34.69456" +
            "      33.04454" +
            "    }" +
            "    {" +
            "      1368894234368" +
            "      E" +
            "      34.694523" +
            "      33.044975" +
            "    }" +
            "    {" +
            "      1368893532346" +
            "      E" +
            "      34.70951" +
            "      33.07644" +
            "    }" +
            "    {" +
            "      1368892931851" +
            "      E" +
            "      34.71363" +
            "      33.161568" +
            "    }" +
            "    {" +
            "      1368892157900" +
            "      E" +
            "      34.7158" +
            "      33.154568" +
            "    }" +
            "    {" +
            "      1368890493586" +
            "      E" +
            "      34.71396" +
            "      33.1624" +
            "    }" +
            "    {" +
            "      1368890074325" +
            "      E" +
            "      34.713997" +
            "      33.147987" +
            "    }" +
            "    {" +
            "      1368889140562" +
            "      E" +
            "      34.713783" +
            "      33.15459" +
            "    }" +
            "    {" +
            "      1368888490709" +
            "      E" +
            "      34.714962" +
            "      33.15596" +
            "    }" +
            "    {" +
            "      1368886441213" +
            "      E" +
            "      34.71432" +
            "      33.154373" +
            "    }" +
            "    {" +
            "      1368885762109" +
            "      E" +
            "      34.712692" +
            "      33.14769" +
            "    }" +
            "    {" +
            "      1368885162028" +
            "      E" +
            "      34.722042" +
            "      33.20204" +
            "    }" +
            "    {" +
            "      1368884562019" +
            "      E" +
            "      34.79542" +
            "      33.346603" +
            "    }" +
            "    {" +
            "      1368884254898" +
            "      E" +
            "      34.85378" +
            "      33.396114" +
            "    }" +
            "    {" +
            "      1368883654307" +
            "      E" +
            "      35.011204" +
            "      33.384514" +
            "    }" +
            "    {" +
            "      1368883053393" +
            "      E" +
            "      35.135033" +
            "      33.359596" +
            "    }" +
            "    {" +
            "      1368882452524" +
            "      E" +
            "      35.13671" +
            "      33.363472" +
            "    }" +
            "    {" +
            "      1368881445724" +
            "      E" +
            "      35.136765" +
            "      33.363613" +
            "    }" +
            "    {" +
            "      1368877818936" +
            "      E" +
            "      35.138725" +
            "      33.36316" +
            "    }" +
            "    {" +
            "      1368874219014" +
            "      E" +
            "      35.136353" +
            "      33.363407" +
            "    }" +
            "    {" +
            "      1368870619043" +
            "      E" +
            "      35.13612" +
            "      33.36347" +
            "    }" +
            "    {" +
            "      1368867020635" +
            "      E" +
            "      35.136143" +
            "      33.363632" +
            "    }" +
            "    {" +
            "      1368863420861" +
            "      E" +
            "      35.13607" +
            "      33.36354" +
            "    }" +
            "    {" +
            "      1368859819200" +
            "      E" +
            "      35.136303" +
            "      33.36356" +
            "    }" +
            "    {" +
            "      1368856219194" +
            "      E" +
            "      35.13639" +
            "      33.363487" +
            "    }" +
            "    {" +
            "      1368841841165" +
            "      H" +
            "      35.136654" +
            "      33.36358" +
            "    }" +
            "    {" +
            "      1368831024035" +
            "      H" +
            "      35.13666" +
            "      33.36362" +
            "    }" +
            "    {" +
            "      1368823842273" +
            "      H" +
            "      35.136646" +
            "      33.363533" +
            "    }" +
            "    {" +
            "      1368819196574" +
            "      H" +
            "      35.13613" +
            "      33.363476" +
            "    }" +
            "    {" +
            "      1368816618645" +
            "      H" +
            "      35.13603" +
            "      33.363335" +
            "    }" +
            "    {" +
            "      1368813555443" +
            "      H" +
            "      35.13615" +
            "      33.36349" +
            "    }" +
            "    {" +
            "      1368813019095" +
            "      H" +
            "      35.136127" +
            "      33.36348" +
            "    }" +
            "    {" +
            "      1368808168609" +
            "      E" +
            "      35.13666" +
            "      33.3633" +
            "    }" +
            "    {" +
            "      1368807946450" +
            "      E" +
            "      35.141937" +
            "      33.359413" +
            "    }" +
            "    {" +
            "      1368805827175" +
            "      E" +
            "      35.142746" +
            "      33.359917" +
            "    }" +
            "    {" +
            "      1368804924936" +
            "      E" +
            "      35.13612" +
            "      33.36347" +
            "    }" +
            "    {" +
            "      1368802228734" +
            "      E" +
            "      35.13637" +
            "      33.363377" +
            "    }" +
            "    {" +
            "      1368800647166" +
            "      E" +
            "      35.13739" +
            "      33.374573" +
            "    }" +
            "    {" +
            "      1368800047186" +
            "      E" +
            "      35.049538" +
            "      33.385525" +
            "    }" +
            "    {" +
            "      1368799607082" +
            "      E" +
            "      34.99072" +
            "      33.460102" +
            "    }" +
            "    {" +
            "      1368799006538" +
            "      W" +
            "      34.96309" +
            "      33.6005" +
            "    }" +
            "    {" +
            "      1368798406566" +
            "      W" +
            "      35.00855" +
            "      33.696396" +
            "    }" +
            "    {" +
            "      1368795019012" +
            "      W" +
            "      35.008133" +
            "      33.697254" +
            "    }" +
            "    {" +
            "      1368790913256" +
            "      W" +
            "      35.00792" +
            "      33.69662" +
            "    }" +
            "    {" +
            "      1368786072408" +
            "      W" +
            "      35.007874" +
            "      33.69714" +
            "    }" +
            "    {" +
            "      1368778900503" +
            "      W" +
            "      35.008026" +
            "      33.697163" +
            "    }" +
            "    {" +
            "      1368772613913" +
            "      E" +
            "      35.00807" +
            "      33.69721" +
            "    }" +
            "    {" +
            "      1368769818925" +
            "      E" +
            "      35.008156" +
            "      33.697258" +
            "    }" +
            "    {" +
            "      1368769059675" +
            "      E" +
            "      35.000927" +
            "      33.687836" +
            "    }" +
            "    {" +
            "      1368768459330" +
            "      E" +
            "      34.96591" +
            "      33.558296" +
            "    }" +
            "    {" +
            "      1368767857186" +
            "      E" +
            "      35.005825" +
            "      33.388798" +
            "    }" +
            "    {" +
            "      1368767257160" +
            "      E" +
            "      35.112667" +
            "      33.35895" +
            "    }" +
            "    {" +
            "      1368766656686" +
            "      H" +
            "      35.136715" +
            "      33.363262" +
            "    }" +
            "    {" +
            "      1368766221770" +
            "      H" +
            "      35.136673" +
            "      33.36362" +
            "    }" +
            "    {" +
            "      1368762621881" +
            "      H" +
            "      35.136623" +
            "      33.363388" +
            "    }" +
            "    {" +
            "      1368755420802" +
            "      H" +
            "      35.13664" +
            "      33.363506" +
            "    }" +
            "    {" +
            "      1368737474741" +
            "      H" +
            "      35.13666" +
            "      33.363594" +
            "    }" +
            "    {" +
            "      1368733818609" +
            "      H" +
            "      35.136826" +
            "      33.363537" +
            "    }" +
            "    {" +
            "      1368732523822" +
            "      H" +
            "      35.136154" +
            "      33.36349" +
            "    }" +
            "    {" +
            "      1368730235736" +
            "      H" +
            "      35.13622" +
            "      33.363533" +
            "    }" +
            "    {" +
            "      1368723056706" +
            "      E" +
            "      35.13602" +
            "      33.363438" +
            "    }" +
            "    {" +
            "      1368719418984" +
            "      E" +
            "      35.13626" +
            "      33.36344" +
            "    }" +
            "    {" +
            "      1368714761930" +
            "      E" +
            "      35.136284" +
            "      33.363518" +
            "    }" +
            "    {" +
            "      1368712218918" +
            "      W" +
            "      35.136044" +
            "      33.363373" +
            "    }" +
            "    {" +
            "      1368711355367" +
            "      W" +
            "      35.113976" +
            "      33.36472" +
            "    }" +
            "    {" +
            "      1368711204349" +
            "      W" +
            "      35.111782" +
            "      33.356277" +
            "    }" +
            "    {" +
            "      1368710604285" +
            "      W" +
            "      34.999516" +
            "      33.40845" +
            "    }" +
            "    {" +
            "      1368710004378" +
            "      W" +
            "      34.95292" +
            "      33.57406" +
            "    }" +
            "    {" +
            "      1368709404106" +
            "      W" +
            "      35.005234" +
            "      33.691685" +
            "    }" +
            "    {" +
            "      1368708618946" +
            "      W" +
            "      35.008175" +
            "      33.697235" +
            "    }" +
            "    {" +
            "      1368705018818" +
            "      W" +
            "      35.00819" +
            "      33.697216" +
            "    }" +
            "    {" +
            "      1368701350057" +
            "      W" +
            "      35.008137" +
            "      33.697235" +
            "    }" +
            "    {" +
            "      1368699388268" +
            "      W" +
            "      35.008167" +
            "      33.697227" +
            "    }" +
            "    {" +
            "      1368697684342" +
            "      W" +
            "      35.008137" +
            "      33.697212" +
            "    }" +
            "    {" +
            "      1368696596914" +
            "      W" +
            "      34.996704" +
            "      33.692917" +
            "    }" +
            "    {" +
            "      1368690747923" +
            "      W" +
            "      35.00815" +
            "      33.697227" +
            "    }" +
            "    {" +
            "      1368687018879" +
            "      E" +
            "      35.008183" +
            "      33.697216" +
            "    }" +
            "    {" +
            "      1368683712561" +
            "      E" +
            "      35.00816" +
            "      33.697323" +
            "    }" +
            "    {" +
            "      1368683092868" +
            "      E" +
            "      34.97342" +
            "      33.658936" +
            "    }" +
            "    {" +
            "      1368682486408" +
            "      E" +
            "      34.989487" +
            "      33.502014" +
            "    }" +
            "    {" +
            "      1368681887419" +
            "      E" +
            "      35.060986" +
            "      33.382706" +
            "    }" +
            "    {" +
            "      1368681287320" +
            "      E" +
            "      35.126938" +
            "      33.356506" +
            "    }" +
            "    {" +
            "      1368681229381" +
            "      E" +
            "      35.130447" +
            "      33.357124" +
            "    }" +
            "    {" +
            "      1368680629260" +
            "      E" +
            "      35.15266" +
            "      33.35913" +
            "    }" +
            "    {" +
            "      1368680028071" +
            "      H" +
            "      35.158073" +
            "      33.35782" +
            "    }" +
            "    {" +
            "      1368679427449" +
            "      H" +
            "      35.136597" +
            "      33.36333" +
            "    }" +
            "    {" +
            "      1368678759279" +
            "      H" +
            "      35.13666" +
            "      33.36357" +
            "    }" +
            "    {" +
            "      1368673439080" +
            "      H" +
            "      35.136715" +
            "      33.363815" +
            "    }" +
            "    {" +
            "      1368661766112" +
            "      H" +
            "      35.13666" +
            "      33.363735" +
            "    }" +
            "    {" +
            "      1368651127327" +
            "      H" +
            "      35.13671" +
            "      33.363716" +
            "    }" +
            "    {" +
            "      1368643837823" +
            "      H" +
            "      35.13624" +
            "      33.36361" +
            "    }" +
            "    {" +
            "      1368639189386" +
            "      H" +
            "      35.136066" +
            "      33.363625" +
            "    }" +
            "    {" +
            "      1368632146260" +
            "      E" +
            "      35.136078" +
            "      33.363407" +
            "    }" +
            "    {" +
            "      1368625857789" +
            "      W" +
            "      35.13675" +
            "      33.36359" +
            "    }" +
            "    {" +
            "      1368622218464" +
            "      W" +
            "      35.13655" +
            "      33.363503" +
            "    }" +
            "    {" +
            "      1368620791186" +
            "      W" +
            "      35.13615" +
            "      33.363285" +
            "    }" +
            "    {" +
            "      1368619802586" +
            "      W" +
            "      35.145752" +
            "      33.359833" +
            "    }" +
            "    {" +
            "      1368619202584" +
            "      W" +
            "      35.160564" +
            "      33.355324" +
            "    }" +
            "    {" +
            "      1368618587004" +
            "      W" +
            "      35.14321" +
            "      33.358963" +
            "    }" +
            "    {" +
            "      1368618340333" +
            "      W" +
            "      35.136715" +
            "      33.363262" +
            "    }" +
            "    {" +
            "      1368615309694" +
            "      W" +
            "      35.13668" +
            "      33.363297" +
            "    }" +
            "    {" +
            "      1368614701001" +
            "      W" +
            "      35.117928" +
            "      33.333366" +
            "    }" +
            "    {" +
            "      1368614100256" +
            "      W" +
            "      35.11489" +
            "      33.332386" +
            "    }" +
            "    {" +
            "      1368613488353" +
            "      W" +
            "      35.11577" +
            "      33.332664" +
            "    }" +
            "    {" +
            "      1368612872173" +
            "      W" +
            "      35.136963" +
            "      33.362667" +
            "    }" +
            "    {" +
            "      1368610574583" +
            "      W" +
            "      35.13628" +
            "      33.363487" +
            "    }" +
            "    {" +
            "      1368607818946" +
            "      W" +
            "      35.13631" +
            "      33.363438" +
            "    }" +
            "    {" +
            "      1368606371478" +
            "      W" +
            "      35.13632" +
            "      33.363426" +
            "    }";
}