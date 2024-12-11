import AdmZip from 'adm-zip'

const zip = new AdmZip()

zip.addLocalFile('./dist/artefact/newArtefactHandler.js')
zip.addLocalFile('./dist/artefact/newArtefactHandler.js.map')
zip.writeZip('./dist/artefactdeploy.zip')

console.log('created ./dist/artefactdeploy.zip')
