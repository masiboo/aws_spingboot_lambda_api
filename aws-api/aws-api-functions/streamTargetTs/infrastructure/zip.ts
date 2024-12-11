import AdmZip from 'adm-zip'

const zip = new AdmZip()

zip.addLocalFile('./dist/stream/enricher.js')
zip.addLocalFile('./dist/stream/enricher.js.map')
zip.addLocalFile('./dist/stream/batchEnricher.js')
zip.addLocalFile('./dist/stream/batchEnricher.js.map')
zip.writeZip('./dist/streamdeploy.zip')

console.log('created ./dist/streamdeploy.zip')
