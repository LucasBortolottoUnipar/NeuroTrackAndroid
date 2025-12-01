package org.gradle.accessors.dm;

import org.gradle.api.NonNullApi;
import org.gradle.api.artifacts.MinimalExternalModuleDependency;
import org.gradle.plugin.use.PluginDependency;
import org.gradle.api.artifacts.ExternalModuleDependencyBundle;
import org.gradle.api.artifacts.MutableVersionConstraint;
import org.gradle.api.provider.Provider;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.internal.catalog.AbstractExternalDependencyFactory;
import org.gradle.api.internal.catalog.DefaultVersionCatalog;
import java.util.Map;
import org.gradle.api.internal.attributes.AttributesFactory;
import org.gradle.api.internal.artifacts.dsl.CapabilityNotationParser;
import javax.inject.Inject;


@NonNullApi
public class LibrariesForLibs extends AbstractExternalDependencyFactory {

    private final AbstractExternalDependencyFactory owner = this;
    private final EspressoLibraryAccessors laccForEspressoLibraryAccessors = new EspressoLibraryAccessors(owner);
    private final ExtLibraryAccessors laccForExtLibraryAccessors = new ExtLibraryAccessors(owner);
    private final VersionAccessors vaccForVersionAccessors = new VersionAccessors(providers, config);
    private final BundleAccessors baccForBundleAccessors = new BundleAccessors(objects, providers, config, attributesFactory, capabilityNotationParser);
    private final PluginAccessors paccForPluginAccessors = new PluginAccessors(providers, config);

    @Inject
    public LibrariesForLibs(DefaultVersionCatalog config, ProviderFactory providers, ObjectFactory objects, AttributesFactory attributesFactory, CapabilityNotationParser capabilityNotationParser) {
        super(config, providers, objects, attributesFactory, capabilityNotationParser);
    }

    
    public Provider<MinimalExternalModuleDependency> getActivity() {
        return create("activity");
    }

    
    public Provider<MinimalExternalModuleDependency> getAppcompat() {
        return create("appcompat");
    }

    
    public Provider<MinimalExternalModuleDependency> getConstraintlayout() {
        return create("constraintlayout");
    }

    
    public Provider<MinimalExternalModuleDependency> getJunit() {
        return create("junit");
    }

    
    public Provider<MinimalExternalModuleDependency> getMaterial() {
        return create("material");
    }

    
    public EspressoLibraryAccessors getEspresso() {
        return laccForEspressoLibraryAccessors;
    }

    
    public ExtLibraryAccessors getExt() {
        return laccForExtLibraryAccessors;
    }

    
    public VersionAccessors getVersions() {
        return vaccForVersionAccessors;
    }

    
    public BundleAccessors getBundles() {
        return baccForBundleAccessors;
    }

    
    public PluginAccessors getPlugins() {
        return paccForPluginAccessors;
    }

    public static class EspressoLibraryAccessors extends SubDependencyFactory {

        public EspressoLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        
        public Provider<MinimalExternalModuleDependency> getCore() {
            return create("espresso.core");
        }

    }

    public static class ExtLibraryAccessors extends SubDependencyFactory {

        public ExtLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        
        public Provider<MinimalExternalModuleDependency> getJunit() {
            return create("ext.junit");
        }

    }

    public static class VersionAccessors extends VersionFactory  {

        public VersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        
        public Provider<String> getActivity() { return getVersion("activity"); }

        
        public Provider<String> getAgp() { return getVersion("agp"); }

        
        public Provider<String> getAppcompat() { return getVersion("appcompat"); }

        
        public Provider<String> getConstraintlayout() { return getVersion("constraintlayout"); }

        
        public Provider<String> getEspressoCore() { return getVersion("espressoCore"); }

        
        public Provider<String> getJunit() { return getVersion("junit"); }

        
        public Provider<String> getJunitVersion() { return getVersion("junitVersion"); }

        
        public Provider<String> getMaterial() { return getVersion("material"); }

    }

    public static class BundleAccessors extends BundleFactory {

        public BundleAccessors(ObjectFactory objects, ProviderFactory providers, DefaultVersionCatalog config, AttributesFactory attributesFactory, CapabilityNotationParser capabilityNotationParser) { super(objects, providers, config, attributesFactory, capabilityNotationParser); }

    }

    public static class PluginAccessors extends PluginFactory {
        private final AndroidPluginAccessors paccForAndroidPluginAccessors = new AndroidPluginAccessors(providers, config);

        public PluginAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        
        public AndroidPluginAccessors getAndroid() {
            return paccForAndroidPluginAccessors;
        }

    }

    public static class AndroidPluginAccessors extends PluginFactory {

        public AndroidPluginAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        
        public Provider<PluginDependency> getApplication() { return createPlugin("android.application"); }

    }

}

