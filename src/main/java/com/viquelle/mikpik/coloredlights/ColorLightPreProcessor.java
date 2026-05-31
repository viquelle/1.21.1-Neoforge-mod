package com.viquelle.mikpik.coloredlights;

import com.viquelle.mikpik.MikpikMod;
import foundry.veil.api.client.render.shader.processor.ShaderPreProcessor;
import io.github.ocelot.glslprocessor.api.GlslInjectionPoint;
import io.github.ocelot.glslprocessor.api.GlslParser;
import io.github.ocelot.glslprocessor.api.GlslSyntaxException;
import io.github.ocelot.glslprocessor.api.grammar.*;
import io.github.ocelot.glslprocessor.api.node.GlslNode;
import io.github.ocelot.glslprocessor.api.node.GlslNodeList;
import io.github.ocelot.glslprocessor.api.node.GlslTree;
import io.github.ocelot.glslprocessor.api.node.constant.GlslFloatConstantNode;
import io.github.ocelot.glslprocessor.api.node.function.GlslFunctionNode;
import io.github.ocelot.glslprocessor.api.node.function.GlslPrimitiveConstructorNode;
import io.github.ocelot.glslprocessor.api.node.variable.GlslNewFieldNode;
import io.github.ocelot.glslprocessor.api.node.variable.GlslVariableDeclarationNode;
import io.github.ocelot.glslprocessor.api.node.variable.GlslVariableNode;
import io.github.ocelot.glslprocessor.api.visitor.GlslNodeVisitor;
import io.github.ocelot.glslprocessor.lib.anarres.cpp.LexerException;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ColorLightPreProcessor implements ShaderPreProcessor {

    public static final ColorLightPreProcessor INSTANCE = new ColorLightPreProcessor();

    private static final ResourceLocation[] TARGET_SHADERS = {
            ResourceLocation.parse("minecraft:shaders/core/rendertype_solid.vsh"),
            ResourceLocation.parse("minecraft:shaders/core/rendertype_solid.fsh")
//            "rendertype_cutout", "rendertype_cutout_mipped",
//            "rendertype_translucent", "rendertype_tripwire"
    };

    private ColorLightPreProcessor() {}

    @Override
    public void modify(Context ctx, GlslTree tree) throws IOException, GlslSyntaxException, LexerException {
        ResourceLocation shaderName = ctx.name();
        boolean isTarget = false;
        for (ResourceLocation target : TARGET_SHADERS) {
            if (shaderName.equals(target)) {
                isTarget = true;
                break;
            }
        }

        if (!isTarget) return;

        MikpikMod.LOGGER.info("Processing shader: {} ({})", shaderName, ctx.type());

        if (ctx.isVertex()) {
            modifyVertexShader(ctx, tree);
        } else if (ctx.isFragment()) {
            modifyFragmentShader(tree);
        }
    }

    private void modifyVertexShader(Context ctx, GlslTree tree) throws GlslSyntaxException, IOException, LexerException {
        updateVersion(tree);
        addNewUniforms(tree);
        addOutputVariables(tree);
        addLightingFunction(ctx, tree);
        modifyVertexMain(tree);
        MikpikMod.LOGGER.info("5 vertex RESULT BOBINA to source string: {}", tree.toSourceString());
    }

    private void modifyFragmentShader(GlslTree tree) throws GlslSyntaxException {
        updateVersion(tree);
        addFragmentInputVariables(tree);
        modifyFragmentMain(tree);
        MikpikMod.LOGGER.info("fragment RESULT BOBINA to source string: {}", tree.toSourceString());
    }

    private void updateVersion(GlslTree tree) {
        tree.getVersionStatement().setVersion(330);
        tree.getVersionStatement().setCore(true);
    }

    private void addNewUniforms(GlslTree tree) throws GlslSyntaxException {
        tree.getBody().add(GlslInjectionPoint.BEFORE_MAIN,
                GlslParser.parseExpression("uniform int u_light_count;"));
        tree.getBody().add(GlslInjectionPoint.BEFORE_MAIN,
                GlslParser.parseExpression("uniform vec4 u_LightData[64];"));
        tree.getBody().add(GlslInjectionPoint.BEFORE_MAIN,
                GlslParser.parseExpression("uniform vec4 u_LightColor[64];"));
    }

    private void addOutputVariables(GlslTree tree) throws GlslSyntaxException {
        tree.getBody().add(GlslInjectionPoint.BEFORE_MAIN,
                GlslParser.parseExpression("out vec4 blockLightColor;"));
    }

    private void addLightingFunction(Context ctx, GlslTree tree) throws GlslSyntaxException, IOException, LexerException {
        boolean exists = tree.functions().anyMatch(func -> "getBlockLightColor".equals(func.getName()));

        if (exists) return;
        ctx.include(tree, ResourceLocation.fromNamespaceAndPath("mikpik", "colored_light"), IncludeOverloadStrategy.SOURCE);
    }


    private void modifyVertexMain(GlslTree tree) throws GlslSyntaxException {
        var mainOpt = tree.mainFunction();
        if (mainOpt.isEmpty()) return;

        GlslFunctionNode main = mainOpt.get();
        GlslNodeList body = main.getBody();

        body.add(GlslParser.parseExpression("blockLightColor = getBlockLightColor(pos, u_light_count, u_LightData, u_LightColor);"));
    }

    private void addFragmentInputVariables(GlslTree tree) throws GlslSyntaxException {
        tree.getBody().add(GlslInjectionPoint.BEFORE_MAIN,
                GlslParser.parseExpression("in vec4 blockLightColor;"));
    }

    private void modifyFragmentMain(GlslTree tree) throws GlslSyntaxException {
        var mainOpt = tree.mainFunction();
        if (mainOpt.isEmpty()) return;

        GlslFunctionNode main = mainOpt.get();
        GlslNodeList body = main.getBody();

        String colorModCode = """
            if (blockLightColor.a > 0.0) {
                color.rgb *= (1.75 * blockLightColor.rgb + vec3(1.0));
            }
            """;

        for (int i = 0; i < body.size(); i++) {
            GlslNode node = body.get(i);

            String src = node.toSourceString();

            if (src.contains("vec4 color")) {
                body.add(i + 1, GlslParser.parseExpression(colorModCode));
                return;
            }
        }

        MikpikMod.LOGGER.warn("Could not find vec4 color declaration in fragment shader");
    }
}