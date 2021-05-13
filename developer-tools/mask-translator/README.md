## About

The MaskTranslator tool is a simple way to translate direction masks into textual degree representations. Slices are added together in the output, with gaps being ignored. For instance if the first three bits are turned on in the mask, we print ['0 - 67.5'] rather than each 22.5 degree slice.

## Running

This script runs with node and can be ran as follows:
```
node ./maskTranslator.js 0001111110000000
```
which returns the following:
[ '67.5 - 202.5' ]