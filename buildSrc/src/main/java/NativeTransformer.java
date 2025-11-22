import com.github.jengelman.gradle.plugins.shadow.transformers.Transformer;
import com.github.jengelman.gradle.plugins.shadow.transformers.TransformerContext;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;
import org.gradle.api.GradleException;
import org.gradle.api.file.FileTreeElement;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class NativeTransformer implements Transformer {
	private final Map<String, String> relocations = new HashMap<>();
	private final HashMap<String, byte[]> rewrittenFiles = new HashMap<>();
	private NativeRelocator relocator;
	private java.io.File rootDir;

	@Override
	public @NotNull String getName() {
		return "NativeTransformer";
	}

	@Override
	public boolean canTransformResource(@Nonnull FileTreeElement element) {
		return relocations.keySet().stream()
				.anyMatch(key -> element.getName().startsWith(key));
	}

	@Override
	public void transform(@Nonnull TransformerContext context) {
		byte[] content;
		try {
			content = context.getIs().readAllBytes();
		} catch (IOException e) {
			throw new GradleException("Failed to read resource content", e);
		}

		// Lazy initialization of nativeRelocator
		if (relocator == null) {
			relocator = new NativeRelocator(rootDir.toPath().resolve("relocate_natives"));
		}

		try {
			// Find the first matching path prefix replacement
			Map.Entry<String, String> pathReplacement = relocations.entrySet().stream()
					.filter(entry -> context.getPath().startsWith(entry.getKey()))
					.findFirst()
					.orElseThrow(() -> new NoSuchElementException("No matching replacement found for path: " + context.getPath()));

			// Apply the path replacement
			String newPath = context.getPath().replace(pathReplacement.getKey(), pathReplacement.getValue());

			// Process the binary with the relocator
			content = relocator.processBinary(newPath, content, relocations);

			// Store the rewritten file
			rewrittenFiles.put(newPath, content);

		} catch (Throwable e) {
			throw new GradleException("Failed to relocate native library: " + context.getPath(), e);
		}
	}

	@Override
	public boolean hasTransformedResource() {
		return !rewrittenFiles.isEmpty();
	}

	@Override
	public void modifyOutputStream(ZipOutputStream os, boolean preserveFileTimestamps) {
		for (Map.Entry<String, byte[]> rewrittenFile : rewrittenFiles.entrySet()) {
            try {
                os.putNextEntry(new ZipEntry(rewrittenFile.getKey()));
				os.write(rewrittenFile.getValue());
            } catch (IOException e) {
                throw new GradleException("Failed to write relocated native library: " + rewrittenFile.getKey(), e);
            }
		}
	}

	// Gradle DSL helper methods
	public void relocateNative(String pattern, String replacement) {
		this.relocations.put(pattern, replacement);
	}

	public void setRootDir(java.io.File rootDir) {
		this.rootDir = rootDir;
	}
}