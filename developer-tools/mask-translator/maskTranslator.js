const translate = (mask) => {
    let returnTranslated = [];
    if (!mask || mask.length !== 16) {
        return returnTranslated;
    }
    let countDegree = 0;
    let currentStart;
    let currentEnd;

    for (let i = 0; i < mask.length; i++) {
        if (mask.charAt(i) === '1') {
            if (currentStart == null) {
                currentStart = countDegree;
            }
            currentEnd = countDegree + 22.5;

            if (i == mask.length - 1) {
                returnTranslated.push(currentStart + " - " + currentEnd);
            }
        }
        else {
            if (currentStart != null) {
                returnTranslated.push(currentStart + " - " + currentEnd);
            }
            currentStart = null;
            currentEnd = null;
        }
        countDegree += 22.5;
    }
    return returnTranslated;
}

function main() {
    var mask = process.argv.slice(2)[0];
    var translation = translate(mask);
    console.log(translation);
}

main();