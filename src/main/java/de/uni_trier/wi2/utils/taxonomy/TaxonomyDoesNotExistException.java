package de.uni_trier.wi2.utils.taxonomy;

public class TaxonomyDoesNotExistException extends RuntimeException {
    TaxonomyDoesNotExistException(String taxonomyName) {
        super(String.format("The taxonomy \"%s\" does not exist!", taxonomyName));
    }
}
