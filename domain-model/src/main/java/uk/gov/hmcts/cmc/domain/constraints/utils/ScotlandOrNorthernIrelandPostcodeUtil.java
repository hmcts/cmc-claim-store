package uk.gov.hmcts.cmc.domain.constraints.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ScotlandOrNorthernIrelandPostcodeUtil {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final String path = "src/main/resources/scotlandandnipostcodes.csv";

    public List<PostcodeDistrict> postcodeDistricts;

    public List<PostcodeDistrict> readPostcodes() {

        List<PostcodeDistrict> postcodeDistricts = new ArrayList<PostcodeDistrict>();

        try (Stream<String> lines = Files.lines(Paths.get(path))) {
            postcodeDistricts = lines.map(line -> {
                PostcodeDistrict postcodeDistrict = new PostcodeDistrict();
                String[] tempPostcodes = line.split(",", -1);
                postcodeDistrict.setScotlandPostcode(tempPostcodes[0]);
                postcodeDistrict.setNorthernIrelandPostcode(tempPostcodes[1]);
                return postcodeDistrict;
            }).collect(Collectors.toList());

        } catch (IOException e) {
            logger.error("Error reading postcode districts", e);
        }

        return postcodeDistricts;
    }

    public boolean postcodeInScotlandOrNorthernIreland(List<PostcodeDistrict> postcodeDistricts, String postcode) {
        return postcodeDistricts
            .stream()
            .anyMatch(postcodeDistrict -> postcodeDistrict.getScotlandPostcode().equals(postcode)
                || postcodeDistrict.getNorthernIrelandPostcode().equals(postcode));
    }
}

class PostcodeDistrict {
    String scotlandPostcode;
    String northernIrelandPostcode;

    public String getScotlandPostcode() {
        return scotlandPostcode;
    }

    public void setScotlandPostcode(String scotlandPostcode) {
        this.scotlandPostcode = scotlandPostcode;
    }

    public String getNorthernIrelandPostcode() {
        return northernIrelandPostcode;
    }

    public void setNorthernIrelandPostcode(String northernIrelandPostcode) {
        this.northernIrelandPostcode = northernIrelandPostcode;
    }
}
