package org.datavec.api.split;

import lombok.NonNull;
import org.nd4j.linalg.collection.CompactHeapStringList;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;

/**
 * InputSplit implementation that maps the URIs of a given BaseInputSplit to new URIs. Useful when features and labels
 * are in different files sharing a common naming scheme, and the name of the output file can be determined given the
 * name of the input file.
 *
 * @author Ede Meijer
 */
public class TransformSplit extends BaseInputSplit {
    private final BaseInputSplit sourceSplit;
    private final URITransform transform;

    /**
     * Apply a given transformation to the raw URI objects
     *
     * @param sourceSplit the split with URIs to transform
     * @param transform transform operation that returns a new URI based on an input URI
     * @throws URISyntaxException thrown if the transformed URI is malformed
     */
    public TransformSplit(@NonNull BaseInputSplit sourceSplit, @NonNull URITransform transform)
                    throws URISyntaxException {
        this.sourceSplit = sourceSplit;
        this.transform = transform;
        initialize();
    }

    /**
     * Static factory method, replace the string version of the URI with a simple search-replace pair
     *
     * @param sourceSplit the split with URIs to transform
     * @param search the string to search
     * @param replace the string to replace with
     * @throws URISyntaxException thrown if the transformed URI is malformed
     */
    public static TransformSplit ofSearchReplace(@NonNull BaseInputSplit sourceSplit, @NonNull final String search,
                    @NonNull final String replace) throws URISyntaxException {
        return new TransformSplit(sourceSplit, new URITransform() {
            @Override
            public URI apply(URI uri) throws URISyntaxException {
                return new URI(uri.toString().replace(search, replace));
            }
        });
    }

    private void initialize() throws URISyntaxException {
        length = sourceSplit.length();
        uriStrings = new CompactHeapStringList();
        Iterator<URI> iter = sourceSplit.locationsIterator();
        while (iter.hasNext()) {
            URI uri = iter.next();
            uri = transform.apply(uri);
            uriStrings.add(uri.toString());
        }
    }

    @Override
    public void write(DataOutput out) throws IOException {

    }

    @Override
    public void readFields(DataInput in) throws IOException {

    }

    @Override
    public void reset() {
        //No op: BaseInputSplit doesn't support randomization directly, and TransformSplit doesn't either
    }

    @Override
    public boolean resetSupported() {
        return true;
    }

    public interface URITransform {
        URI apply(URI uri) throws URISyntaxException;
    }
}
